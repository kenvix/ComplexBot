namespace java com.kenvix.complexbot.rpc.thrift

struct TextClassificationResult {
    string name;
    double proba;
}

exception RPCException {
     1: optional string message;
     2: optional string name;
     3: optional RPCException cause;
}

service BackendBridge {
    string ping(1:string data) throws (1: RPCException e)
    string operate(1:string operate) throws (1: RPCException e)

    string getAboutInfo() throws (1: RPCException e)
    i32 getBackendVersionCode() throws (1: RPCException e)

    string parseCaptchaFromFile(1:string path) throws (1: RPCException e)
    string parseCaptchaFromBinary(1:binary data) throws (1: RPCException e)

    TextClassificationResult classificateTextMessage(1:string text) throws (1: RPCException e)

    string ocrFromFile(1:string path) throws (1: RPCException e)
    string ocrFromBinary(1:binary data) throws (1: RPCException e)

    string scanQrCodeFromFile(1:string path) throws (1: RPCException e)
    string scanQrCodeFromBinary(1:binary data) throws (1: RPCException e)
}