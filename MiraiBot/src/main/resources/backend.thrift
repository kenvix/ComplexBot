service BackendBridge {
    string ping(1:string data)
    string operate(1:string operate)

    string parseCaptchaFromFile(1:string path)
    string parseCaptchaFromBinary(1:binary data)

    string classificateTextMessage(1:string text)

    string ocrFromFile(1:string path)
    string ocrFromBinary(1:binary data)

    string scanQrCodeFromFile(1:string path)
    string scanQrCodeFromBinary(1:binary data)
}