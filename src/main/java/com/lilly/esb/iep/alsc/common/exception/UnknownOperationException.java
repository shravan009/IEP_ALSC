package com.lilly.esb.iep.alsc.common.exception;

@Deprecated
public class UnknownOperationException extends RuntimeException {
    
    private static final long serialVersionUID = -4315524459532357766L;

    public UnknownOperationException() {
        super();
    }
    
    public UnknownOperationException(String s) {
        super(s);
    }
}
