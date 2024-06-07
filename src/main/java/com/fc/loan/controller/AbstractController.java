package com.fc.loan.controller;

import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.dto.ResultObject;

/**
 * 요청에 대한 응답값을 통일화 하기 위해 설정해 놓은 추상 클래스 컨트롤러 <br/>
 * 해당 추상클래스를 일반 컨트롤러 클래스에서 상속받음으로써 <br/>
 * Response해당 클래스를 컨트롤러에서 매번 객체를 생성하지 않고 <br/>
 * 부모 추상클래스의 메소드를 호출함으로써 한번 거쳐 사용하는듯 하다. <br/>
 * 또한 APIExceptionHandler에서 생성자를 통해 exception객체를 ResultObject객체로 초기화하여 필드에 주입할때 사용된다.
 */
public abstract class AbstractController {

  protected <T> ResponseDTO<T> ok() {
    return ok(null, ResultObject.getSuccess());
  }

  protected <T> ResponseDTO<T> ok(T data) {
    return ok(data, ResultObject.getSuccess());
  }

  protected <T> ResponseDTO<T> ok(T data, ResultObject result) {
    ResponseDTO<T> obj = new ResponseDTO<>();
    obj.setResult(result);
    obj.setData(data);

    return obj;
  }
}