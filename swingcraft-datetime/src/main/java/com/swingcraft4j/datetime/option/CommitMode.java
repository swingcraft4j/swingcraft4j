package com.swingcraft4j.datetime.option;

/**
 * What a field does with what is typed when it loses the focus and has no value: it is filled in part,
 * has a date that does not exist, or its validator has an error.
 */
public enum CommitMode {

    /**
     * What is typed stays in the field, and the field has no value.
     */
    KEEP,

    /**
     * The field goes back to the value it had before it was edited. If it had none, it is cleared.
     */
    REVERT,

    /**
     * The field is cleared.
     */
    CLEAR
}
