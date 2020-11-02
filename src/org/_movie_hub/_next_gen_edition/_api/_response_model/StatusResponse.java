package org._movie_hub._next_gen_edition._api._response_model;

/**
 * @author Mandela aka puumInc
 */
public enum StatusResponse {

    SUCCESS ("Success"), ERROR ("Error"), WARNING("Warning");

    public String status;

    StatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
