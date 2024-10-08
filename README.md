# 배포 환경
<details>
<summary>접기/펼치기</summary>
컨테이너 기반 환경인 Docker와 K8S를 함께 사용한다.  
### 컨테이너란?  
 - 소프트웨어 서비스를 실행하는 데 필요한 특정 버전의 프로그래밍 언어 및 라이브러리와 같은 종속 항목과 어플리케이션 코드를 함께 포함하는 경량 패키지를 말한다.  
 - 동일한 어플리케이션이지만 각 작업 환경마다 새로운 이슈가 발생한다면?  
   → 로컬 환경에서 실행할 때와 개발 환경에서 실행할때, 그리고 실제 운영되는 상용 환경에서 실행될 때 그리고 다른 작업의 로컬 환경에서 실행할 때
   각각의 환경에서 인지하지 못했던 새로운 이슈가 발생할 수 있다.
- 어플리케이션에 필요한 구성을 패키지로 묶어, 운영 환경이 달라져도 동일하게 실행될 수 있도록 제공해주는 이점이 존재한다.

### Kubernetes, K8S 란?
 - 컨테이너들을 배포하고 관리해 주는 플랫폼을 말한다.

# 구축 가이드

<details>
<summary>접기/펼치기</summary>


## Docker
 - [Docker Install Mac](https://docs.docker.com/desktop/install/mac-install/)
 - [Docker Install Windows](https://docs.docker.com/desktop/install/windows-install/)

위 링크에서 DockerDektop을 설치한다.  
(Windows라면 가상환경을 위한 WSL이 선행 설치되어 있어야 한다.)

## Kubernetes
 - 도커 쿠버네티스 활성화   
  Settings | Preferences (톱니바퀴) `>` Kubernetes `>` [x] Enable Kubernetes 체크  `>` [ Apply & restart ]  

## Kuberctl
쿠버네티스에 대한 컨트롤을 한다.
- [Kuberctl Install Mac](https://kubernetes.io/ko/docs/tasks/tools/install-kubectl-macos/)
- [Kuberctl Install Windows](https://kubernetes.io/ko/docs/tasks/tools/install-kubectl-windows/)

  ### Windows Chocolatey 기준 설치 및 학인 명령  
  (chocolatey가 선행 설치되어 있어야 한다.)
   - Kuberctl 설치
      ```bash
      choco install kubernetes-cli
      ```
   - Kuberctl 버전 확인
      ```bash
      kubectl version --client
      ```
      ```text/plain
      WARNING: This version information is deprecated and will be replaced with the output from kubectl version --short.  Use --output=yaml|json to get the full version.
      Client Version: version.Info{Major:"1", Minor:"27", GitVersion:"v1.27.2", GitCommit:"7f6f68fdabc4df88cfea2dcf9a19b2b830f1e647", GitTreeState:"clean", BuildDate:"2023-05-17T14:20:07Z", GoVersion:"go1.20.4", Compiler:"gc", Platform:"windows/amd64"}
      Kustomize Version: v5.0.1
      ```
   - Kuberctl 서비스 조회
      ```bash
      kubectl get svc
      ```
      ```text/plain
      NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
      kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   5m23s
      ```
</details>

## JIB를 통한 Docker Image 생성

### JIB란?
- Java 어플리케이션에 최적화 된 Docker 및 OCI 이미지를 빌드해주는 툴
1. #### build.gradle에 plugin 추가
    ```json
    plugins {
        id 'org.springframework.boot' version '2.7.1'
        id 'io.spring.dependency-management' version '1.0.11.RELEASE'
        id 'com.google.cloud-tools.jib' version '3.2.0' // 플러그인 추가
        id 'java'
    }
    /*생략*/
    ```
2. #### gradle refresh  
   jib 태스크가 추가된 것 확인.


3. #### build.gradle에 jib 정의  
    - from: 어떠한 베이스 이미지를 기준으로 어플리케이션에 대한 이미지를 생성할 것인지
    - to: 생성할 이미지
    - container: 
      - mainClass : 생성한 이미지 서버의 어플리케이션 클래스 패스 정의
      - creationTime : 생성 시간에 대한 TimeStamp 설정
      - format : 
    ```json
    /*생략*/
    repositories {/*생략*/}
    jib {
        from {
            image = 'openjdk:11-jre-slim'
        }
        to {
            image = 'fc-loan'
            tags = ['0.0.1']
        }
        container {
            mainClass = 'com.fc.load.LoanApplication'
            creationTime = 'USE_CURRENT_TIMESTAMP'
            format = 'OCI'
            volumes = ['/var/tmp']
            entrypoint = [
                'java',
                'cp',
                '/app/resources:/app/classes:/app/libs/*',
                'com.fc.load.LoanApplication'
            ]
        }
    }
    /*생략*/
    dependencies {/*생략*/}
    ```
4. #### H2DB Mysql로 수정
    일반적으로 작성하듯 url에 localhost라고 작성한다면, docker에서는 docker내부의 localhost를 바라보게 되기 때문에  
    `localhost` 대신 `host.docker.internal`을 사용한다.
    ```yaml
    spring:
      datasource:
        driverClassName: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://host.docker.internal:3306/load?characterEncoding=UTF-8&serverTimezone=Asia/Seoul
        username: root
        password: 1234
    jpa:
    # 생략
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    database: mysql
    ```
5. DB 접속 및 load database 생성
   ```bash
   mysql -uroot -p
   ```
   ```bash
   show databases;
   ```
   ```bash
   create database load;
   ```
   ```bash
   show databases;
   ```
6. Gradle `>` Tasks `>` lib `>` jibDockerBuild 클릭
7. docker image 확인
    ```bash
    docker images
    ```
8. docker image 실행
    ```bash
    docker run -ti fc-loan /bin/bash
    ```

## Skaffold를 통한 Kubernetes 배포
### Skaffold란?
코드 수정이 K8S에 반영되기 전까지의 과정을 자동화하여 단순화 해주는 프레임워크이다.  
[Scaffold Link](https://skaffold.dev/) `>` Get Scaffold for CLI `>` Windows `>` 최 하단 Chocolatey

### Chocolatey를 통한 scaffold 설치
  ```bash
  choco install -y skaffold
  ```
### skaffold 관련 yaml 설정 파일 정의
Skaffold를 사용하기 위해서는 skaffold yaml 파일을 정의해 줘야 한다.  
jib를 통해 생성한 Docker 이미지를 실행하는 설정을 정의한다.
- 최상위디렉토리/skaffold.yml
  ```yaml
  apiVersion: skaffold/v4beta2
  kind: Config
  build:
    artifacts:
      - image: fc-loan
        jib: {}
  ```
K8S에 배포하기 위해 K8S에 존재하는 여러가지 오브젝트들 중 deployment와 service 설정을 정의해야한다.  
정의 해 놓은 설정 값들을 통해 Local K8S 환경에 Object(deployment, service 등)들이 생성된다.
- 최상위디렉토리/k8s/deployment.yml
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: fc-loan
  spec:
    selector:
      matchLabels:
        app: fc-loan
      template:
        metadata:
          labels:
            app: fc-loan
        spec:
          containers:
            - name: fc-loan
              image: fc-loan
              ports:
                - containerPort: 8080 
                # Pod를 실행 할 때 어떤 스펙의 컨테이너를 실행할 지에 대해 설정을 진행하게 된다.
                # Pod란 Kubernetes의 배포와 관리의 기본 단위이다. (자세한 내용은 chatGPT에게...)
                # 서비스에 지정한 targetPort와 실제로 사용하는 컨테이너 포트를 일치시켜 줘야 매칭 될 수 있다.
                # (이렇게 매칭 시켜줘야 컨테이너로 트래픽이 전달될 수 있다)
  ```
- 최상위디렉토리/k8s/service.yml
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: fc-loan
    namespace: default
    labels:
      app: fc-loan
  spec:
    selector:
      app: fc-loan
    ports:
      - name: http
        port: 8080
        targetPort: 8080
    type: LoadBalancer
  ```
### 이미지 빌드 및 쿠버네티스 환경에 배포
  ```bash
  skaffold dev
  ```
### Postman을 통한 테스트 진행하기
로컬에서 서버를 띄우지 않은 상태로 localhost:8080 url 그대로 진행한다. 
</details>

# *핀테크 및 대출 도메인 이해*
<details>
<summary>접기/펼치기</summary>


## 핀테크란

`금융(Finance)` + `기술(Technology)`의 합성어
### 금융 이란?
자본에 이자를 부쳐 돈이 필요한 곳에 자본을 빌려주는 행워  
돈에 여유가 있는 사람이 쉽게 돈을 빌려주는 과정이 반복되는 행위  
은행, 증권, 보험회사 등 여러 시장들에서는 자본을 모집하고, 모집된 자본을 다른 시장에 빌려주는 모습이 반복되는 것이 금융산업이다.

시간이 어느덧 흘러 아날로그식 수기로 흘러가던 금융산업에 IT산업이 더해지게 된다.  
IT산업은 우리 삶에서 뗄래야 뗄 수 없는 이미 삶 자체가 되어버렀다.  
우리는 하루 종일 스마트폰만 보고 있을 때도 있고, 스마트폰으로 모든 것을 다 해결할 수 있는 시대에 살고 있다.  
이렇게 우리, 나 자신 자체가 되어버린 것 같은 IT산업은 금융산업에도 IT가 손을 뻗게 된다.

금융의 본질은 단순하다고 생각할 수 있다.  
그러나 자본에이자를 붙여 돈이 필요한 곳에 빌려주는 이러한 금융산업의 응용 분야는 굉장히 광활하다.

- Fintech
    - 송금
    - 결제
    - 자산관리
    - 투자

금융이라는 키워드 아래 많은 분야들이 존재한다.  
국내 핀테크 스타트업의 수만 보더라도 2020년 말 기준 484개나 된다고 한다.  
이러한 사례로 우리는 은행에 직접 가지 않고 온라인으로 송금을 하고, 내 계좌를 개설하고,
나도 전혀 몰랐던 소비패턴도 알게되고, 내 소비패턴에 적절한 금융상품을 소개받는 등 삶속에서 손쉽게 금융을 접하고 있다.

- 기대와 달리 Fin + 人(사람)인 경우가 많은 현실
- 기존 프로세스를 시스템화
- 비즈니스 이해를 통한 새로운 가치 창출
- 막대한 시간과 인력 투자의 필요성

하지만 핀테크라고 생각했던 우리의 기대와 달리 핀 플러스 사람인 경우가 굉장히 많은 현실을 우리는 살고 있다.  
사람만으로는 못하던 기술을 통해 새로운 상품과 프로세스를 만들어가는 즉, 새로운 가치 창출이라는 이상적인 그림을 꿈꾸었지만, 현실과 이상적인 것은 괴리가 있을 수 있다.  
금융산업을 이해함과 동시에 과거부터 운영되어 온 프로세스를 시스템화 하고 새로운 가치를 창출하는 게 정말 어려운 부분이기 때문이다.  
막대한 시간을 쏟을 수 있고, 인력을 원하는 만큼 뽑을 수 있는 이상적인 그림을 꿈꾼다면 될까 말까 일 것 같은데,  
아무튼 시스템화 하고 혁신적인 아이디어를 구체화하여 아웃풋을 낼 수 있는 여건과 역량이 부족한 것이 현재 핀테크 시장이 아닐까 싶다.  
(물론 굉장히 잘 해나가는 회사도 있음.)  
핀테크 산업 전반적으로 고민하기에는 이 시간이 너무 추상적인 것 같다.  
개발자이고 개발자를 희망하는 사람들이기에 현실적으로 고민해 볼 법한 부분은 무엇이 있을까?  
개발자가 어떤 역할을 해서 핀테크에서 테크를 완성해 나갈 수 있을지에 대해서 고민해봐야 될 필요성이 있다.

- 혁신적인 아이디어는 많다
- 어떻게 요건을 현실화하여 Output을 만들어 낼 수 있는지가 핵심
- 개발자가 어떤 역할을 해서 Fintech 산업에서 Tech 를 완성?

세상에 혁신적인 아이디어는 굉장히 많다.  
그러나 생각에 그치지 않고 어떻게 요건을 현실화하여 아웃풋을 만들어낼 수 있는지가 굉장히 중요하다.  
백날 생각해봤자 이를 현실적으로 만들 수 없으면 의미없는 생각이 될 수 있다.  
아웃풋을 만들어내는 역할은 누가 가장 잘할 수 있을까?  
바로 개발자이다.  
우리는 어떻게 개발자가 어떤 역할을 해서 핀테크 산업에서 테크를 완성해나갈 수 있을지에 대해서 고민을 해봐야 한다.

- 어떻게 Tech를 완성할 수 있는가?
- 많은 Fintech 영역 회사들이 요구하는 조건을 보자
-
어떻게 Fintech를 완성할 수 있을까?  
많은 Fintech 영역 회사들은 어떤 개발자를 원하고 있을까?  
좋은 분들과 함께 일하기 위한 마음의 원하는 인재상이 있으며, 같이 일하고 싶은 동료들의 이러한 역량과 경험이 있으면 좋겠다 라고 하는 것의 집약체가 `Job Description` 이다.  
이러한 `Job Description`을 분석해보는 것도 핀테크 분야에서 원하는 개발자의 조건과 역량을 직접적으로 파악할 수 있는 좋은 방법일 것이다.

#### 실제 특정 회사의 `Job Description` 예시 1
- Java Kotlin중 하나 이상의 언어에 능숙하신 분이 필요해요.
- **주어진 비즈니스에 대한 이해가 빠르고, 이에 필요한 시스템의 설계가 가능하신 분이 필요해요.**
- 성능 최적화와 운영 자동화를 위해 지속적인 노력을 하는 분이 필요해요.
- 글로벌 서비스 런칭 경험이 있는 분이면 좋아요.

#### 실제 특정 회사의 `Job Description` 예시 2
- Go, Java, Kotlin, Python 중 하나 이상의 프로그래밍 언어를 이용해 서버 애플리케이션을 개발해본 경험이 요구됩니다.
- **프로젝트 진행에 있어특정 프로그램이 언어나 기술만을 고지하기보다는 합리적인 기술 의사 결정을 내릴 수 있는 유연함이 요구됩니다.**
- **다양한 직군과 원활한 의사소통이 가능하신 분을 찾고 있습니다.**
- 약 2년 이상의 서버 애플리케이션 개발 경력을 보유하신 분 혹은 그에 준하는 역량을 갖추신 분이어야 합니다.

<br>

- 반드시 최신 기술 학습만이 정답일까? **NO!**
- 내가 개발할 **도메인 이해**의 중요성
- 문제 해결을 위한 합리적인 의사결정

물론 최신 기술 학습도 중요하다. 빠르게 발전하고 있는 현실에 맞춰서 더 좋은 것을 배워서 적용할 수 있으면 당연히 좋다.  
그러나 이보다 더 중요한 것이 내가 개발할 도메인을 이해하고 있는가 이다.  
개발자는 문제 해결에 포커싱을 가지고 있어야 한다.  
단순히 기술을 추구하고 개발자 자신이 보기에 뿌듯한 서비스를 만든다고 해도 이는 의미가 없다.  
방망이 깎는 노인 처럼 장인정신으로써 소스코드를 만들어서 세상에 오픈한다고 해도 세상에서는 알아주지 않는?  
쉽게말해 돈을 벌지 못하는 서비스를 만든다고 하면 과연 의미가 있을까?  
단순히 기술을 추구하고 개발자 자신이 보기에 뿌듯한 서비스라고 해도 이게 세상에서 가치를 낼 수 없으면 의미가 없다.  
백날 리팩토링하고 코드 구조를 잘 짜서 만들더라도 돈을 벌지 못하는 서비스라면 의미 없는 서비스로서 비춰질 수 있기 때문이다.  
즉 우리는 문제 해결을 위해 합리적인 의사결정을 할 수 있는 역량 또한 중요하다.  
기술적인 부분이 대한 고민과 비즈니스에 대한 고민이 적절하게 협의를 해 이뤄서 합리적으로 의사결정을 할 수 있도록 판단할 수 있는 역량이 중요하다는 의미이다.


- 알고 있는가?
    - 내가 무엇을 만들고 있는지?
    - 내가 무엇을 만들고 싶은지?
    - 내가 왜 만들고 있는지?
    - 내가 왜 만들어야 하는지?
    - 내가 만드는 Output이 어떤 Impact 를 낼 수 있는지?
- Fintech 분야를 떠나 모든 개발자들에게 필요

아무런 의심 없이 호기심 없이 만들라고 하는 대로 만드는 수동적인 마인드는 개발자로서 피해야 될 마인드이다.  
우리는 왜 만들고 무엇을 만들어야 되는지 인지하고 있는 상태에서 개발을 해야 한다.  
예를들어 *"위에서 하라고 해서 만들었어요" "이 기능 뭐에요?" "그냥 저는 만들라고 해서 만들었는데요?"* 이런 부분들은 굉장히 피해야 되는 부분이다.
물론 금융에는 이것이 굉장히 어려울 수 있다.  
금융은 정말 단순하게 정의할 수 있다고 하더라도 굉장히 어려운 분야이다.  
그러나 이해하기 위한 노력을 계속해서 반복해야만 한다.  
이해하기 위해 전문가들을 찾아 의견을 구하고 인터넷에서 서칭도 해보고 이해를 계속해서 높여 감으로써 우리가 만들고 있는 아웃풋에 대해서 더욱 더 완성도 있고 가치 있는 산출물로 나올 수 있도록 계속해서 노력 해야 한다.

</details>

# *대출 도메인 프로세스 및 Flow*

<details>
<summary>접기/펼치기</summary>

대출은 무조건 신청을 한다고 해서 받는것은 아니다.  
대출 요건이라던지, 금리라던지, 대출에 필요한 서류라던지 이러한 부분들을 상담을 통해 받아볼 수 있을것이다.  
나에게 적합한 대출 상품인지? 내가 이 조건에 해당하는지? 등 어떤 것들이 필요한지 미리 알 수 있어야 한다.  
사실 신청과 직접적으로 연관되어 있는 과정이라고 보기엔 어렵지만 대출 상담도 대출 프로세스의 과정 중 하나이다.

1. `신청` [YES/NO]
2. `약관 동의`
3. `대출 신청서 작성`  
   (+ 희망한도)
4. `신청 조건` [YES/NO]
5. `입회 서류 등록`
6. `대출 신청`

그래서 대출 상담을 받고 상담을 받은 다음 대출을 신청하고자 결심한 차주(돈을 빌리고자 하는 사람)이 대출 신청을 하거나 아니면 상담 조건을 받은 뒤 마음에 별로 안들어 나한테는 돈이 안나올것 같아
이렇게 생각할 수 있다.  
두 가지 선택 길에서 `대출 신청을 한다`고 했을 때 대출을 받기 위한 `약관을 동의`하고
나는 이정도 `대출 한도`가 나와야 될 것 같은데 그래서 `대출 신청서를 작성`하고 `신청 조건`에 맞는지 안맞는지 결정에 따라 대출 신청에 필요한 `입회 서류`를 등록하는 과정을 거친 뒤 `대출 신청`을 하게 된다.

- `신청 조건` → `대출 조건 제시`

`4.` 과정의 신청 조건을 돈을 빌려주는 기관에서 확인을 할것이다.  
돈을 갚을 수 있는지에 대해 조건을 까다롭게 봐야 될 것이다.  
만약 대출 신청의 조건에 맞을 때 이사람은 못갚을 것 같아 라고 판단되면 신청 프로세스는 종료될 것이며, 갚을 수 있을것이라 판단되면 대출 조건을 제시하게 될 것이다.
- `조건 수락` → `대출 약정` → `대출금 집행` → `차주`

위와 같은 심사 과정을 거쳐 대출을 받고자 하는 사람입장에서 `조건을 수락`하거나 돈 이거밖에 안돼? 난 더 필요한데? 어? 이자가 더 높은데? `조건 거절` 두가지  선택을 할 수 있다.  
조건을 수락한다고 했을 때, 내가 언제부터 언제까지 돈을 빌려서 얼마의 이자를 매번 내면서 갚겠다는 `대출 약정`을 맺게 되고 이후 실제로 돈을 빌려주게 될것이다.  
이를 `대출금을 집행`한다고 표현 한다.

이후 부터는 대출금이 집행이 되고 차주는 정기적으로 대출금을 상한 하게 될것이다.  
대출 상한금과 이자를 더해 꼬박꼬박 갚으면 집행 됐던 최초 잔고에서 조금씩 대출금을 상환하게 될것이다.  
최족적으로 대출 상한을 완료하면 채무이행 관계가 끝남으로써 대출이 상한완료 되고 프로세스가 종료되게 된다.
</details>

# *대출 상담 도메인*

## 상담이란
"문제를 해결하거나 궁금증을 풀기 위하여 서로 의논함" `[출저: 네이버 국어사전]`   
대출을 받을 때도, 대출을 받기 위해 필요한 서류 혹은 조건 등등에 대한 궁금증을 풀기 위하여 상담이 필요할 수 있다.
상담에서 끝날 수도 있고, 상담에 이어 대출을 실제로 신청하는 과정까지 이어질 수 있다.  
어디까지나 처음 진입하는 단계라고 볼 수 있는 상담이 대출 프로세스의 필수 과정이라고 볼수는 없다.  
바로 대출신청을 할 수 있기 때문이다.  
하지만 무언가를 신청할 때 상담이 필요한 경우는 반드시 있다.  
이에 대출 상담 기능을 만들어 본다.

### 도메인 테이블 정의

#### [상담] 테이블
- **ID** : Long(BIGINT)
- **Soft Delete** : 물리적 삭제가 아닌 논리삭제 (Update로 상태 변경)
- **DATETIME** 데이터 생성/수정 시점을 시스템상 남기기 위해 시분초 포함
    - ***`TIMESTAMP`*** 고려 가능하나 국내 한정으로 대출신청이 들어올것이라 제한.  
      (UTC 지원이 가능한 TimeZone에 의존하는 타입으로 보통 글로벌 서비스에 고려해서 사용하면 좋다.  
      물론 국내 한정이라도 타임스탬프를 사용해도 전혀 문제는 없다.)


- Table 정의
  ```sql
  CREATE TABLE counsel (
    counsel_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
    is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일자',
    address VARCHAR(50) DEFAULT NULL COMMENT '주소',
    address_detail VARCHAR(50) DEFAULT NULL COMMENT '상세주소',
    applied_at DATETIME DEFAULT NULL COMMENT '신청일자',
    cell_phone VARCHAR(13) DEFAULT NULL COMMENT '전화번호',
    email VARCHAR(50) DEFAULT NULL COMMENT '상담 요청자 이메일',
    memo DEFAULT NULL COMMENT '상담 메모',
    name VARCHAR(12) DEFAULT NULL COMMENT '상담 요청자',
    zip_code VARCHAR(5) DEFAULT NULL COMMENT '우편번호',
    primary key (counsel_id)
  )
  ```
- Entity 정의
  ```java
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @SQLDelete(sql = "UPDATE counsel SET deleted_at = NOW() where counsel_id = ?")
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class Counsel extends BaseEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long counselId;
      @Column(nullable = false, columnDefinition = "DATETIME DEFAULT NULL COMMENT '신청일자'")
      private LocalDateTime appliedAt;
      @Column(nullable = false, columnDefinition = "VARCHAR(12) COMMENT '상담 요청자'")
      private String name;
      @Column(nullable = false, columnDefinition = "VARCHAR(23) COMMENT '전화번호'")
      private String cellPhone;
      @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '상담 요청자 이메일'")
      private String email;
      @Column(columnDefinition = "TEXT DEFAULT NULL COMMENT '상담 메모'")
      private String memo;
      @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '주소'")
      private String address;
      @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '상세주소'")
      private String address_detail;
      @Column(columnDefinition = "VARCHAR(5) DEFAULT NULL COMMENT '우편번호'")
      private String zipCode;
  }
  ```



# *대출 상담 등록 기능 구현*

### URL(POST)
```text
https://localhost:8080/counsels
```

### Request - CounselDTO.Request
```json
{
  "name": "김아무",
  "cellPhone": "010-1111-2222",
  "email": "yoohyeok@school.com",
  "memo": "대출 상담을 원합니다 .",
  "address": "서울 어딘구 여기동",
  "addressDetail": "123-45",
  "zipCode": "11122"
}
```

### Response - ResponseDTO<CounselDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "counselId": 1,
    "name": "김아무",
    "cellPhone": "010-1111-2222",
    "email": "yoohyeok@school.com",
    "memo": "대출 상담을 원합니다 .",
    "address": "서울 어딘구 여기동",
    "addressDetail": "123-45",
    "zipCode": "11122",
    "appliedAt": "2024-06-07T00:20:44.500463",
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *대출 상담 조회 기능 구현*

### URL(GET)
```text
https://localhost:8080/counsels/{counselId}
```

### PathVariable - counselId
```text
Long : 1
```

### Response - ResponseDTO<CounselDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "counselId": 1,
    "name": "김아무",
    "cellPhone": "010-1111-2222",
    "email": "yoohyeok@school.com",
    "memo": "대출 상담을 원합니다 .",
    "address": "서울 어딘구 여기동",
    "addressDetail": "123-45",
    "zipCode": "11122",
    "appliedAt": "2024-06-07T00:20:44.500463",
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *대출 상담 수정 기능 구현*

### URL(PUT)
```text
https://localhost:8080/counsels/{counselId}
```

### PathVariable - counselId
```text
Long : 1
```

### Request - CounselDTO.Request
```json
{
  "name": "박아무",
  "cellPhone": "010-3333-4444",
  "email": "yoohyeok@school.com",
  "memo": "대출 상담을 원합니다 . 2(메모 수정)",
  "address": "서울 어딘가구 여기동",
  "addressDetail": "923-45",
  "zipCode": "33322"
}
```

### Response - ResponseDTO<CounselDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "counselId": 1,
    "name": "박아무",
    "cellPhone": "010-3333-4444",
    "email": "yoohyeok@school.com",
    "memo": "대출 상담을 원합니다 . 2(메모 수정)",
    "address": "서울 어딘가구 여기동",
    "addressDetail": "923-45",
    "zipCode": "33322",
    "appliedAt": "2024-06-07T00:20:44.500463",
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *대출 상담 삭제 기능 구현*

### URL(DELETE)
```text
https://localhost:8080/counsels/{counselId}
```

### PathVariable - counselId
```text
Long : 1
```

### Response - ResponseDto<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data" : null
}
```

# *대출 신청 프로세스 및 Flow*

1. **대출 신청**  
   고객이 대출 신청을 하게된다  
   물론 단순 변심에 의해 바로 종료가 될 가능성도 있다.
2. **약관동의**  
   대출신청 후 관련된 약관을 확인하고 동의하는 절차를 가진다.
3. **대출 신청서 작성**(+ 희망한도)  
   대출금을 희망하는 금액까지 같이 작성한다.
4. **신청 조건**  
   필수는 아니겠으나 기관에서 먼저 사전적으로 대출신청이 가능한 사람인지 여부를 확인할 수 있다.   
   혹은 시스템상 특정 금액 이상 혹은 미만에 대한 대출금은 신청할 수 없도록 하는 로직단에서도 자동으로 대출 신청 조건에 대해 부결 시킬 수 있다.
5. **입회 서류 등록**  
   실제 대출 심사를 위해 대출 신청자의 모든 조건을 확인해야 하는 시간이 있다.  
   신청과 관련된 입회 서류를 등록하는 절차도 추가 요구사항으로 포함될 수 있다.
6. **대출 신청**  
   최종 대출 신청을 하면 대출 심사를 기다리는 한 프로세스로 대출신청이 진행 된다.

### 도메인 키워드
- 신청서 작성
- 약관 등록
- 심사에 필요한 서류 제출
- 약관 등록은 분리하여 구현
- 신청정보 작성과 파일 업로드도 분리하여 구현
- 신청 조건에 따른 신청 상태 변경도 분리하여 구현

### 도메인 테이블 정의

#### [신청] 테이블
- **ID** : Long(BIGINT)
- **Soft Delete** : 물리적 삭제가 아닌 논리삭제 (Update로 상태 변경)
- 신청을 위해 필요한 기본 필드 정의


- Table 정의
  ```sql
  CREATE TABLE application (
    application_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일자',
    applied_at DATETIME DEFAULT NULL COMMENT '신청일자',
    cell_phone VARCHAR(13) DEFAULT NULL COMMENT '전화번호',
    email VARCHAR(50) DEFAULT NULL COMMENT '상담 요청자 이메일',
    fee DECIMAL(5,4) DEFAULT NULL COMMENT '주소',
    hope_amount DECIMAL(15,2) DEFAULT NULL COMMENT '상세주소',
    interest_rate DECIMAL(5,4) DEFAULT NULL COMMENT '상담 메모',
    maturity DATETIME DEFAULT NULL COMMENT '만기',
    name VARCHAR(12) DEFAULT NULL COMMENT '신청자',
    primary key (application_id)
  )
  ```
- Entity 정의
  ```java
    @Entity
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamicInsert
    @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
    @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
    public class Application extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long applicationId;
    @Column(columnDefinition = "DATETIME DEFAULT NULL COMMENT '신청일자'")
    private LocalDateTime appliedAt;
    @Column(columnDefinition = "VARCHAR(12) DEFAULT NULL COMMENT '신청자'")
    private String name;
    @Column(columnDefinition = "VARCHAR(23) DEFAULT NULL COMMENT '전화번호'")
    private String cellPhone;
    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '신청자 이메일'")
    private String email;
    @Column(columnDefinition = "DECIMAL(5,4) DEFAULT NULL COMMENT '취급수수료'")
    private BigDecimal fee;
    @Column(columnDefinition = "DECIMAL(15,2) DEFAULT NULL COMMENT '대출 신청 금액'")
    private BigDecimal hopeAmount;
    @Column(columnDefinition = "DECIMAL(5,4) DEFAULT NULL COMMENT '금리'")
    private BigDecimal interestedRate;
    @Column(columnDefinition = "DATETIME DEFAULT NULL COMMENT '만기'")
    private LocalDateTime maturity;
    }
  ```

# *대출 신청 등록 기능 구현*

### URL(POST)
```text
https://localhost:8080/applications
```

### Request - ApplicationDTO.Request
```json
{
  "name": "김아무",
  "cellPhone": "010-1111-2222",
  "email": "yoohyeok@school.com",
  "hopeAmount": "50000000"
}
```

### Response - ResponseDTO<ApplicationDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "applicationId": 1,
    "name": "김아무",
    "cellPhone": "010-1111-2222",
    "email": "yoohyeok@school.com",
    "hopeAmount": "50000000",
    "appliedAt": "2024-06-07T00:20:44.500463",
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *대출 신청 조회 기능 구현*

### URL(GET)
```text
https://localhost:8080/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<ApplicationDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "applicationId": 1,
    "name": "김아무",
    "cellPhone": "010-1111-2222",
    "email": "yoohyeok@school.com",
    "hopeAmount": "50000000",
    "appliedAt": "2024-06-07T00:20:44.500463",
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *대출 상담 수정 기능 구현*

### URL(PUT)
```text
https://localhost:8080/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - ApplicationDTO.Request
```json
{
  "name": "수정아무",
  "cellPhone": "010-3333-4444",
  "email": "update@success.com",
  "hopeAmount": "0"
}
```

### Response - ResponseDTO<ApplicationDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "applicationId": 1,
    "name": "수정아무",
    "cellPhone": "010-3333-4444",
    "email": "update@success.com",
    "hopeAmount": 0,
    "appliedAt": "2024-06-13T00:32:16.39708",
    "createdAt": "2024-06-13T00:32:16.437887",
    "updatedAt": "2024-06-14T00:03:46.6237035"
  }
}
```

# *대출 신청 삭제 기능 구현*

### URL(DELETE)
```text
https://localhost:8080/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data" : null
}
```

# 이용 약관
대출신청 후 관련된 약관을 확인하고 동의하는 절차를 가진다.
어떠한 특정 서비스를 이용하고자 할 때 약관에 대해서 동의를 한다.  
시스템 상에서 특정 약관에 대해 내려주기 위해서는 약관을 등록하고 조회할 수 있는 기능이 필요하다.  
보통은 고객이 등록한다는 것 보다는 내부 직원이 약관 관리자가 약관을 등록하는 형태가 될것이다.  
우리가 개발 했던 대출 신청을 예로 들면 `대출 신청 이용 약관`을 등록해야 되는 경우가 있고 해당 약관이 존재해야  
약관 신청 / 약관 동의를 할 수 있기 때문에 우선적으로 약관을 등록할 수 있는 기능을 구현해야 한다.

- 약관 등록 및 관리
- 최대한 간단하게, 세부 내용은 URL로 제공 (URL 까지 만들지는 않음)

---

- 테이블 정의
    ```sql
    CREATE TABLE terms (
        terms_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        name VARCHAR(255) NOT NULL COMMENT '약관',
        terms_detail_url VARCHAR(255) NOT NULL COMMENT '약관상세 URL',
        PRIMARY KEY (terms_id)
    )
    ```

- Entity 정의
  ```java
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class TERMS extends BaseEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long termsId;
      @Column(columnDefinition = "VARCHAR(255) DEFAULT NULL COMMENT '약관'")
      private String name;
      @Column(columnDefinition = "VARCHAR(255) DEFAULT NULL COMMENT '약관상세 URL'")
      private String termsDetailUrl;
    }
  ```


# *이용 약관 등록 기능 구현*

### URL(POST)
```text
https://localhost:8080/terms
```

### Request - TermsDTO.Request
```json
{
  "name": "김아무",
  "termsDetailUrl": "https://abc-storage.acc/exampletest"
}
```

### Response - ResponseDTO<TermsDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:": {
    "termsId": 1,
    "name": "신청 이용 약관",
    "termsDetailUrl": "https://abc-storage.acc/exampletest"
    "createdAt": "2024-06-07T00:20:44.533554",
    "updatedAt": "2024-06-07T00:20:44.533554"
  }
}
```

# *이용 약관 전체 조회 기능 구현*

### URL(GET)
```text
https://localhost:8080/terms
```

### Response - ResponseDTO<TermsDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data:":[
    {
      "termsId": 1,
      "name": "신청 이용 약관",
      "termsDetailUrl": "https://abc-storage.acc/exampletest"
      "createdAt": "2024-06-07T00:20:44.533554",
      "updatedAt": "2024-06-07T00:20:44.533554"
    },
    {
      "termsId": 2,
      "name": "신청 이용 약관",
      "termsDetailUrl": "https://abc-storage.acc/exampletest"
      "createdAt": "2024-06-07T00:20:44.533554",
      "updatedAt": "2024-06-07T00:20:44.533554"
    }
  ]
}
```
---
# *대출 신청 이용 약관*
이용 약관에 대해 등록/조회 할 수 있는 기능을 구현 했다.  
구현된 기능을 통해 등록되어 있는 `이용 약관`을 대출 신청 페이지에서 내려주면  
그 약관을 대출 신청을 하는 신청자가 보고 동의를 했을 때 동의한 정보를 기록하기 위한 기능을 추가한다.

- **`신청 정보와 약관 매핑`**  
  특정 신청자가 신청 정보의 약관에 동의를 했다
- **`최대한 간단하게 구현`**
- **`존재하는 모든 약관을 동의 했는가? 검증 필요`**  
  예를 들어 3개의 약관 중 2개의 약관만 동의하여 서버로 요청한다면,  
  (물론 Front 영역에서 Validation을 잘 해놨겠을 것이다.)
  따라서 Back단에서 존재하는 모든 약관을 동의 했는가에 대한 검증이 필요함.
- **`대출 신청과 관련되어 있으므로 Application 서비스에서 기능 구현`**  
  대출 신청에 대한 이용 약관 이므로, 주종 관계가 성립된다.  
  **Entity 연관관계**
    - 주인: 대출 신청 - Application
    - 종: 이용 약관 - Terms
- **`이중 관계로 인한 중간 테이블 AcceptTerms 필요`**  
  특정 대출 신청에 특정 이용 약관이 동의되면 대출 신청 PK와 이용 약관 PK가 저장된다.  
  N:M 관계 이므로 1:N **↔** N:1 관계로 풀어내야 한다.
    - 하나의 대출신청이 여러개의 이용악관을 동의할 수 있음
    - 하나의 이용약관이 여러개의 대출신청에서 동의할 수 있음

---
- 테이블 정의
    ```sql
    CREATE TABLE terms (
        acceptTerms_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        application_id BIGINT NOT NULL COMMENT '대출 신청 ID',
        terms_id BIGINT NOT NULL COMMENT '이용 약관 ID',
        PRIMARY KEY (terms_id)
    )
    ```

- Entity 정의
  ```java
  @ToString
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class AcceptTerms extends BaseEntity {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long acceptTermsId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
      private Long applicationId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '이용 약관 ID'")
      private Long termsId;
  }

  ```

## *대출 신청 이용 약관 등록 기능*
#### ※약관(Terms) 데이터가 2개 존재해야한다 !!※

### URL(POST)
```text
https://localhost:8080/applications/{applicationId}/terms
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - AppicationDTO.AcceptTerms
```json
{
  "acceptTermsIds" : [1, 2]
}
```

### Response - ResponseDTO<Boolean>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": true
}
```
---

# *대출 신청 서류*
대출 신청을 할 때 심사가 필요한 서류들이 존재할 수 있다.  
대출을 신청받고자 하는 사람 혹은 법인에게 "대출을 해줄 수 있겠다" 라는 판단을 할수 있는 자료로써  
서류를 요구하는 경우들이 종종 존재한다.

## *대출 신청 서류 등록 기능*

### URL(POST)
```text
https://localhost:8080/applications/{applicationId}/files
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - FormData

| key   | value   |
|-------|---------|
| files | 2 files |

### Response - ResponseDTO<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": null
}
```

## *대출 신청 서류 조회 기능*

### URL(GET)
```text
https://localhost:8080/applications/{applicationId}/files?fileName=파일명.확장자
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - FormData

| key      | value   |
|----------|---------|
| filename | 파일명.확장자 |

### Response - ResponseEntity<Resource>

```text

파일이 인코딩되어 깨진 글씨들로 가득하다.

우측 ... 버튼 Save response to file 을 통해 직접 다운받는다.

```

## *대출 신청 서류 압축파일 조회 기능*

### URL(GET)
```text
https://localhost:8080/applications/{applicationId}/zipFiles?fileNames=파일명1.확장자,파일명2.확장자
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - FormData

| key      | value             |
|----------|-------------------|
| fileNames | 파일명1.확장자,파일명2.확장자 |

### Response - ResponseEntity<Resource>

```text

파일이 인코딩되어 깨진 글씨들로 가득하다.

우측 ... 버튼 Save response to file 을 통해 직접 다운받는다.

```
## *대출 신청 입회 서류 파일정보 전체 조회 기능*
업로드 되어있는 디렉토리에 존재하는 모든 입회서류 파일에 대한 전체 정보를 조회하여 반환한다.  
`파일명`, `다운로드 리소스 URL`

### URL(GET)
```text
https://localhost:8080/applications/{applicationId}/files/infos
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<List<FileDTO\>\>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": [
    {
      "name": "Devops프로필.jpg",
      "url": "http://localhost:8080/applications/files?fileName=Devops프로필.jpg"
    },
    {
      "name": "king.png",
      "url": "http://localhost:8080/applications/files?fileName=king.png"
    }
  ]
}
```

## *대출 신청 입회 서류 파일 전체 삭제 기능*
업로드 되어있는 디렉토리에 존재하는 모든 입회서류 파일들을 삭제한다.  
FileSystemUtils 의 deleteRecursively() 메소드를 호출함으로써 삭제한다.  
매개변수로는 `File` 혹은 `Path` 타입으로 받게된다.

### URL(DELETE)
```text
https://localhost:8080/applications/{applicationId}/files
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": null
}
```
## 대출 신청 입회 서류 매핑
신청 서류가 업로드되는 디렉토리에 신청 정보에 대한 Unique ID를 Depth로 넣어서 경로를 신청 정보 ID를 통해 알 수 있도록  
규칙을 정해놓고 해당하는 디렉토리로 신청 서류가 올라갈 수 있고 다운받을 수 있도록 기능을 개선한다.  
ex) `C:\upload\{applicationId}\파일명.확장자`


### 수정 내역
- 입회서류 다중 파일 서버 업로드 (등록)
- 입회서류 파일 로컬 다운로드 (조회)
- 입회서류 압축 파일 로컬 다운로드 (조회)
- 입회서류 파일 정보 전체 조회 (조회)
- 입회서류 파일 전체 삭제 (삭제)

위 항목들의 PathVariable로 applicationId 즉 대출 신청 테이블의 고유 값을 넘겨 각각의 업/다운로드 경로 하위에
고유값을 디렉토리로 한번 더 구성한다.

### URL
```text
https://localhost:8080/applications/{applicationId}/**
```

#### 만약 상위 디렉토리가 존재하지 않는다면 (uploadPath) NosuchFileException이 발생한다.

---
# 대출 심사
`대출 상담`, `대출 신청`, `이용약관`, `입회 서류` 등의 정보를 통해 실제로 대출 심사를 하기 위한 기능을 구현한다.  
심사란 말 그대로 신청한 정보를 기준으로 대출을 실제로 집행해 줘도 되는가  
돈을 빌려줘도 되는가, 빌려주는 측 입장에서 심사라는 과정을 말한다.

- 테이블 정의
    ```sql
    CREATE TABLE judgment (
        judgment_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        application_id BIGINT NOT NULL COMMENT '신청 ID',
        approval_amount DECIMAL(15, 2) DEFAULT NULL COMMENT '승인 금액',
        name VARCHAR(12) NOT NULL COMMENT '심사자',
        PRIMARY KEY (judgment_id)
    )
    ```

- Entity 정의
  ```java
  import java.math.BigDecimal;   
  @ToString
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class Judgment extends BaseEntity {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long judgmentId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
      private Long applicationId;
  
      @Column(columnDefinition = "VARCHAR(12) NOT NULL COMMENT '심사자'")
      private String name;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '승인 금액'")
      private BigDecimal approvalAmount;
  }

  ```

## *대출 심사 등록 기능*

### URL(POST)
```text
https://localhost:8080/judgments
```

### Request - JudgmentDTO.Request
```json
{
  "applicationId" : "1",
  "name" : "김아무",
  "approvalAmount" : "5000000"
}
```

### Response - ResponseDTO<JudgmentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "judgmentId": 1,
    "applicationId": 1,
    "name": "김아무",
    "approvalAmount": 5000000,
    "createdAt": "2024-06-24T00:38:32.7178963",
    "updatedAt": "2024-06-24T00:38:32.7178963"
  }
}
```

## *대출 심사 조회 기능 (심사 id 기준)*

### URL(GET)
```text
https://localhost:8080/judgments/{judgmentId}
```

### PathVariable - judgmentId
```text
Long : 1
```

### Response - ResponseDTO<JudgmentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "judgmentId": 1,
    "applicationId": 1,
    "name": "김아무",
    "approvalAmount": 5000000,
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 심사 조회 기능 (대출 신청 id 기준)*

### URL(GET)
```text
https://localhost:8080/judgments/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<JudgmentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "judgmentId": 1,
    "applicationId": 1,
    "name": "김아무",
    "approvalAmount": 5000000,
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 심사 수정 기능*

### URL(PUT)
```text
https://localhost:8080/judgments/{judgmentId}
```

### PathVariable - judgmentId
```text
Long : 1
```

### Request - JudgmentDTO.Request
```json
{
  "applicationId" : "1",
  "name" : "김아무",
  "approvalAmount" : "10000000" // 변경할 값
}
```

### Response - ResponseDTO<JudgmentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "judgmentId": 1,
    "applicationId": 1,
    "name": "김아무", // 기존 값은 "유 아무" 였음
    "approvalAmount": 10000000, // 기존 값은 5000000임
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 심사 삭제 기능*

### URL(DELETE)
```text
https://localhost:8080/judgments/{judgmentId}
```

### PathVariable - judgmentId
```text
Long : 1
```

### Response - ResponseDTO<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": null
}
```

## *대출 심사 금액 부여 기능*
대출 심사를 한 뒤, 승인 된 금액 까지 심사자를 통해 결정이 되었다.  
실시간으로 반영이 바로 된다기보다 최종 결정권자 즉, 대출 집행과 관련 해서 결정을 최종 승인하는 사람을 통해
실제로 부여되는 프로세스가 있을 것이다.  
심사자를 통해 실시간으로 바로 승인이 되는 경우도 있을 수 있겠지만, 심사자가 심사는 따로 하고 최종적으로 반영 되는 부분을
구분해서 구현해 본다.

### URL(PATCH)
```text
https://localhost:8080/judgments/{judgmentId}/grant
```

### PathVariable - judgmentId
```text
Long : 1
```

### Response - ResponseDTO<JudgmentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "applicationId": 1,
    "approvalAmount": 10000000, // judgment의 approvalAmount로 세팅 수정
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

# 대출 집행

### 집행이란?
- 실제로 시행하는 것을 의미

### 계약을 한 뒤 대출금을 집행해줄 차례

대출 조건을 제안한 정보, 대출금을 가지고 최종적으로 동의를 했을 때 신청한 사람이 계약을 체결하게 된다.  
그래서 계약을 하고 빌려주는 사람이 대출금을 실제로 집행해주는 프로세스 기능을 구현해 본다.  
따라서 집행은 결국 현재까지 만들어 온 대출 신청과 심사 과정이 모두 끝난 후 돈을 빌리고자 한 사람과 계약을 하고, 대출금을 실제로 주는 과정이라고 봐야 한다.  
예를 들어 대출을 500만원 신청했다고 했을 때 계약을 맺고 이후에 500만원을 빌려 준 다음 정기적인 일정에 맞춰 빌린 사람은 돈을 갚아 나가야 할것이다.  
돈을 빌려준 사람 입장에서는 순수하게 빌려준 돈만 받지는 않을 것이다.  
원금만 갚지는 않을것이라는 뜻이고, 서로 계약한 정책에 따라 상환금과 이자를 같이 갚아 나가야 할 것이다.

이번 파트는 대출 계약을 하고, 대출금을 집행한 후 대출 현황까지 관리할 수 있는 대출과 관련되어 있는 마지막 단계의 첫 시작이 될것이다.


## *대출 집행 도메인*
특정 `대출 신청` 정보에 연결되어 있는 집행 정보인지 알아야 하므로 신청 ID `application_id` 를 필드로 추가한다.  
또한 실제로 얼마를 집행 했는지 금액을 알 수 있도록 `entry_amount`필드를 정의한다.  
대출 집행을 할 때 예를 들어 500만원을 대출 신청을 했을 때 회차에 따라 100만원씩 5번을 대출금을 집행할 경우도 있을 수 있고,  
혹은 한번에 일괄적으로 500만원을 집행할 경우도 있을 수 있다.  
따라서 사실 대출 신청 정보 그니까 대출 계약은 N개의 집행 정보가 생성될 수 있다.  
1회에 1번 집행해주는 컨셉으로 구현을 이어나가 본다.

- 테이블 정의
    ```sql
    CREATE TABLE entry (
        entry_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        application_id BIGINT NOT NULL COMMENT '신청 ID',
        entry_amount DECIMAL(15, 2) DEFAULT NULL COMMENT '집행 금액',
        PRIMARY KEY (entry_id)
    )
    ```

- Entity 정의
  ```java
  import java.math.BigDecimal;   
  @ToString
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class Entry extends BaseEntity {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long entryId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
      private Long applicationId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '집행 금액'")
      private BigDecimal entryAmount;
  }

  ```

## *대출 잔고 도메인*
대출금을 집행한 뒤 빌린 사람이 돈을 상환하는 과정까지 해서 너가 돈을 얼마를 빌렸는데 지금 남은 잔여 대출 금액은 얼마인가? 라는 대출 상환 히스토리를 알아야 한다.  
따라서 현재 만드는 테이블의 경우 얼마의 대출을 집행 했고, 얼마를 갚아서 집행할 수 있는 즉, 실제로 남은 대출 금액이 얼마인지 까지만 관리할 수 있는 테이블이다.  
얼만큼을 언제 상환했는지에 대한 추적 같은 경우에는 여러 방식으로 관리할 수 있다.
이벤트 트리거를 심어두고, 테이블의 로우가 업데이트 라던지 어던 변경작업이 발생했을 때의 이력들을 하나씩 쌓음으로써 관리를 할 수도 있고, 혹은 동일한 테이블에서 관리를 할 수도 있다.  
이처럼 방식은 여러가지이지만, 이러한 방식은 일단 뒤로 빼두고 현재 구현하는 부분에서는 집행을 했고 상환을 했을 때, 네가 신청한 대출 우리가 계약한 대출이 얼만큼 금액이 남았어? 에 대해서만 집중하도록 한다.

- 테이블 정의
    ```sql
    CREATE TABLE balance (
        balance_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        application_id BIGINT NOT NULL COMMENT '신청 ID',
        balance DECIMAL(15, 2) DEFAULT NULL COMMENT '잔여 대출 금액',
        PRIMARY KEY (balance_id)
    )
    ```

- Entity 정의
  ```java
  import java.math.BigDecimal;   
  @ToString
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class Balance extends BaseEntity {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long balanceId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
      private Long applicationId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '잔여 대출 금액'")
      private BigDecimal balance;
  }

  ```

## *대출 계약 기능*

### URL(PUT)
```text
https://localhost:8080/applications/{applicationId}/contract
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<ApplicationDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": null
}
```

## *대출 집행 등록 기능*
처음 도메인 설계시 정의한 대로 일반적으로 대출이 집행된 후 대출 금액중 일부를 상환해 나가야 한다.  
따라서 대출금액이 얼마나 남았는지 대출 잔고를 저장할 엔티티를 설게했었다.     
대출 등록 기능에서는 대출을 집행한 뒤 대출을 등록(Entry)하는것 뿐만 아니라 집행된 대출에 대한 잔고(Balance-entryAmount)도 함께 저장한다.

### URL(POST)
```text
https://localhost:8080/internal/applications/{applicationId}/entries
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - EntryDTO.Request
```json
{
  "entryAmount": 5000000
}
```

### Response - ResponseDTO<EntryDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "entryId": 1,
    "applicationId": 1,
    "entryAmount": 5000000,
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 집행 조회 기능*

### URL(GET)
```text
https://localhost:8080/internal/applications/{applicationId}/entries
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<EntryDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "entryId": 1,
    "applicationId": 1,
    "entryAmount": 5000000.00,
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 집행 수정 기능*
구현 의도: 고객에게 돈이 전달되기 전 수행하는 방향으로 구현한다.  
대출 집행 금액이 변경됨에 따라 밸런스의 값도 같이 수정되어야 할것이다.  
집행 아이디를 기준으로 변경할 수 있도록 구현한다.
생성할 때와 그 다음에 엔트리에 대한 수정을 할때에 대한 DTO는 각각 다를 수 있다.  
예를 들어 어떤 금액으로 entryAmount를 수정할거야 라고 했을 때
as-is(현재)의 entryAmount와  to-be(미래) entryAmount가 같이 비교가 된 상태로(비교할 수 있도록) Response DTO를 추가 구성한다.

### URL(PUT)
```text
https://localhost:8080/internal/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - EntryDTO.Request
```json
{
  "entryAmount": 5000000
}
```

### Response - ResponseDTO<EntryDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "entryId": 1,
    "applicationId": 1,
    "entryAmount": 5000000,
    "createdAt": "2024-06-25T02:57:52.459969",
    "updatedAt": "2024-06-25T02:57:52.459969"
  }
}
```

## *대출 집행 삭제 기능*
집행된 정보가 잘못 되어 있어 다시 검토하거나, 혹은 계약 체결 후 집행 전 취소 등  
여러 이유로 인해 삭제 기능이 필요하다.  
대출 집행에서 삭제 기능을 구현하면 Entry가 삭제되었을 때 Balance도 함께 0원으로 초기화 세팅하는 형식으로 진행한다.

### URL(DELETE)
```text
https://localhost:8080/internal/applications/{applicationId}
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<Void>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": null
}
```

# 대출 상환 도메인
대출 상담 → 대출 신청 → 대출금 심사 요건 OK FLOW를 거친 후  
빌려주는 측 에서 빌리고자 하는 사람 혹은 기관에게 대출금을 집행을 하고  
이후 집행된 대출금을 빌린 사람은 상환을 하면서 빌린 금액을 모두 갚아야 하는 Next Step이 있다.      
대출 상환을 할 때 한 번에 갚을 수도 있겠지만 여러 회에 걸쳐 갚을 수도 있을것이며, 또 빌려준 금액에 이자를 더해서 돈을 갚아야 한다.  
빌린사람 입장에서는 대출금을 매 회 상환 해야 될 것이고, 얼만큼의 대출 잔액이 남아있는지,  
언제 몇회에 걸쳐서 대출금을 갚았는지에 대해서 이력을 기능으로 제공 해 줘야만 대출 프로세스에 대한 기능의 마무리를 할 수 있다.

- 테이블 정의
    ```sql
    CREATE TABLE repayment (
        repayment_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자',
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRNET_TIMESTAMP NOT NULL COMMENT '수정일자',
        is_deleted BIT DEFAULT FALSE NOT NULL COMMENT '이용가능여부',
        application_id BIGINT NOT NULL COMMENT '신청 ID',
        repayment_amount DECIMAL(15, 2) DEFAULT NULL COMMENT '상환 금액',
        PRIMARY KEY (repayment_id)
    )
    ```

- Entity 정의
  ```java
  import java.math.BigDecimal;   
  @ToString
  @Entity
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamicInsert
  @DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
  @Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
  public class Repayment extends BaseEntity {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(nullable = false, updatable = false)
      private Long repaymentId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
      private Long applicationId;
  
      @Column(columnDefinition = "BIGINT NOT NULL COMMENT '상환 금액'")
      private BigDecimal repaymentAmount;
  }

  ```

## *대출 상환 등록 기능*

### URL(POST)
```text
https://localhost:8080/internal/applications/{applicationId}/repyments
```

### PathVariable - applicationId
```text
Long : 1
```

### Request - RepaymentDTO.Request
```json
{
  "repaymentAmount": 1000000
}
```

### Response - ResponseDTO<RepaymentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": {
    "repaymentId": 1,
    "applicationId": 1,
    "repaymentAmount": 1000000, // 1000000이 상환됨.
    "balance": 4000000.00, // 상환 후 잔고
    "createdAt": "2024-07-17T02:57:52.459969",
    "updatedAt": "2024-06-17T02:57:52.459969"
  }
}
```

## *대출 상환 조회 기능*
등록 한 상환 정보가 잘못되었을 때 상환 데이터를 수정한다.   
상환 정보에 대해 수정하는 행위이므로 그에 맞게 잔고(Balance)도 다시 조정한다.
### URL(GET)
```text
https://localhost:8080/internal/applications/{applicationId}/repyments
```

### PathVariable - applicationId
```text
Long : 1
```

### Response - ResponseDTO<List<RepaymentDTO.Response>>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": [
    {
      "repaymentId": 1,
      "repaymentAmount": 1000000.00, // 1000000이 상환됨.
      "createdAt": "2024-07-18T02:57:52.459969",
      "updatedAt": "2024-07-18T02:57:52.459969"
    },
    {
      "repaymentId": 2,
      "repaymentAmount": 1000000.00, // 1000000이 상환됨.
      "createdAt": "2024-07-18T02:57:52.459969",
      "updatedAt": "2024-07-18T02:57:52.459969"
    }
  ]
}
```

## *대출 상환 수정 기능*

### URL(PUT)
```text
https://localhost:8080/internal/applications/{repaymentId}/repyments
```

### PathVariable - repaymentId
```text
Long : 1
```

### Request - RepaymentDTO.Request
```json
{
  "repaymentAmount": 2000000
}
```

### Response - ResponseDTO<RepaymentDTO.Response>
```json
{
  "result": {
    "code": "0000",
    "desc": "success"
  },
  "data": [
    {
      "repaymentId": 1,
      "beforeRepaymentAmount": 1000000.00, // 이전 상환액 1000000이 상환 취소됨.
      "updateRepaymentAmount": 2000000.00, // 2000000 으로 상환됨.
      "balance": 3000000.00, // 잔고가 4000000 에서 3000000으로 수정됨.
      "createdAt": "2024-07-18T02:57:52.459969",
      "updatedAt": "2024-07-18T02:57:52.459969"
    }
  ]
}
```

## *대출 상환 삭제 기능*

### URL(DELETE)
```text
https://localhost:8080/internal/repyments/{repaymentId}
```

### PathVariable - repaymentId
```text
Long : 1
```

### Response - ResponseDTO<Void>
```json
{
    "result": {
        "code": "0000",
        "desc": "success"
    },
    "data": null
}
```