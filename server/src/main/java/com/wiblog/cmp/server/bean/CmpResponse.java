package com.wiblog.cmp.server.bean;

/**
 * @author pwm
 * @date 2020/8/30
 */
public class CmpResponse {

    private StatusType statusType;
    private Object entity;

    protected CmpResponse(StatusType statusType, Object entity) {
        this.statusType = statusType;
        this.entity = entity;
    }

    protected CmpResponse(int status, Object entity) {
        this.statusType = toStatusType(status);
        this.entity = entity;
    }

    public StatusType getStatusType() {
        return this.statusType;
    }

    public int getStatus() {
        return this.statusType.getStatusCode();
    }

    public Object getEntity() {
        return this.entity;
    }

    public static ResponseBuilder status(int status) {
        ResponseBuilder b = ResponseBuilder.newInstance();
        b.status(status);
        return b;
    }

    public static ResponseBuilder ok() {
        ResponseBuilder b = ResponseBuilder.newInstance();
        b.status(Status.OK);
        return b;
    }

    public static ResponseBuilder ok(Object entity) {
        ResponseBuilder b = ok();
        b.entity(entity);
        return b;
    }



    public static StatusType toStatusType(final int statusCode) {
        switch (statusCode) {
            case 200:
                return Status.OK;
            case 201:
                return Status.CREATED;
            case 202:
                return Status.ACCEPTED;
            case 204:
                return Status.NO_CONTENT;

            case 301:
                return Status.MOVED_PERMANENTLY;
            case 303:
                return Status.SEE_OTHER;
            case 304:
                return Status.NOT_MODIFIED;
            case 307:
                return Status.TEMPORARY_REDIRECT;

            case 400:
                return Status.BAD_REQUEST;
            case 401:
                return Status.UNAUTHORIZED;
            case 403:
                return Status.FORBIDDEN;
            case 404:
                return Status.NOT_FOUND;
            case 406:
                return Status.NOT_ACCEPTABLE;
            case 409:
                return Status.CONFLICT;
            case 410:
                return Status.GONE;
            case 412:
                return Status.PRECONDITION_FAILED;
            case 415:
                return Status.UNSUPPORTED_MEDIA_TYPE;
            case 500:
                return Status.INTERNAL_SERVER_ERROR;
            case 503:
                return Status.SERVICE_UNAVAILABLE;

            default: {
                return new StatusType() {
                    @Override
                    public int getStatusCode() {
                        return statusCode;
                    }

                    @Override
                    public Status.Family getFamily() {
                        return toFamilyCode(statusCode);
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "";
                    }
                };
            }
        }
    }

    public static Status.Family toFamilyCode(int statusCode) {
        switch(statusCode / 100) {
            case 1:
                return Status.Family.INFORMATIONAL;
            case 2:
                return Status.Family.SUCCESSFUL;
            case 3:
                return Status.Family.REDIRECTION;
            case 4:
                return Status.Family.CLIENT_ERROR;
            case 5:
                return Status.Family.SERVER_ERROR;
            default:
                return Status.Family.OTHER;
        }
    }

    public static class ResponseBuilder {

        private static ResponseBuilder b;

        private StatusType statusType;
        private Object entity;

        protected static ResponseBuilder newInstance() {
            if (b == null) {
                b = new ResponseBuilder();
            }
            return b;
        }

        public ResponseBuilder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        public CmpResponse.ResponseBuilder status(StatusType status) {
            if (status == null) {
                throw new IllegalArgumentException();
            }
            this.statusType = status;
            return this;
        }



        public ResponseBuilder status(int status) {
            return status(CmpResponse.toStatusType(status));
        }

        public CmpResponse build() {
            final CmpResponse r = new CmpResponse(
                    statusType,
                    entity);
            reset();
            return r;
        }

        private void reset() {
            statusType = Status.NO_CONTENT;
            entity = null;
        }
    }

    public interface StatusType {

        int getStatusCode();

        Status.Family getFamily();

        String getReasonPhrase();

    }
    enum Status implements StatusType{
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NO_CONTENT(204, "No Content"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        SEE_OTHER(303, "See Other"),
        NOT_MODIFIED(304, "Not Modified"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        CONFLICT(409, "Conflict"),
        GONE(410, "Gone"),
        PRECONDITION_FAILED(412, "Precondition Failed"),
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable");

        private final int code;
        private final String reason;
        private Family family;

        public enum Family {INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, OTHER}

        Status(final int statusCode, final String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            switch(code/100) {
                case 1: this.family = Family.INFORMATIONAL; break;
                case 2: this.family = Family.SUCCESSFUL; break;
                case 3: this.family = Family.REDIRECTION; break;
                case 4: this.family = Family.CLIENT_ERROR; break;
                case 5: this.family = Family.SERVER_ERROR; break;
                default: this.family = Family.OTHER; break;
            }
        }

        @Override
        public Family getFamily() {
            return family;
        }

        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public String getReasonPhrase() {
            return toString();
        }

        @Override
        public String toString() {
            return reason;
        }

        public static Status fromStatusCode(final int statusCode) {
            for (Status s : Status.values()) {
                if (s.code == statusCode) {
                    return s;
                }
            }
            return null;
        }
    }
}
