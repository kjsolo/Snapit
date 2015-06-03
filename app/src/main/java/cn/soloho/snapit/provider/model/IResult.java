package cn.soloho.snapit.provider.model;

import java.util.List;

public interface IResult<T> {

    boolean getState();
    String getError();
    int getCode();
    int getTotal();

    T getData();
    List<T> getList();

}
