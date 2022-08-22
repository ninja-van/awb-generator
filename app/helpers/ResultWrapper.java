package helpers;

import lombok.Getter;

@Getter
public class ResultWrapper<T> {

    private T result;

    public ResultWrapper(T result) {
        this.result = result;
    }
}
