//web implementation of the Fire-IO service and protocol
function FireIoClient(host, port) {
    const _host = host;
    const _port = port;
    let _password = "";
    let _ws;
    let _token;
    let _connected = false;
    const _events = {};

    const err = function (err) {
        console.error("[Fire-IO] " + err)
    };

    this.setPassword = function (npass) {
        _password = npass;
        return this;
    };

    this.connect = function () {
        const request = new XMLHttpRequest();
        request.open('GET', 'http://' + _host + ":" + _port + "/fireio/register?p=" + _password, true);
        request.onload = function (a) {
            const response = a.target.responseText;
            if (response === "ratelimit") {
                err("The server denied the request due to the rate limiter. please retry later.");
                return;
            }
            if (response === "fail-auth") {
                err("The server denied the request due to a authentication problem. is your password correct?");
                return;
            }
            if (response.length > 8) {
                //ok, try to get the token to use
                _token = response.split("INFO:")[0];
                //setup websocket
                _ws = new WebSocket("ws://" + _host + ":" + (_port + 1) + "/" + _token);
                _ws.onclose = function () {
                    _connected = false;
                    _triggerEvent("close", null);
                    _triggerEvent("disconnect", null);
                };
                _ws.onerror = function (onclose) {
                    console.log("err: " + onclose);
                };
                _ws.onmessage = function (onclose) {
                    console.log("mess: " + onclose);
                };
                _ws.onopen = function () {
                    _connected = true;
                    _triggerEvent("connect", null);
                    _triggerEvent("open", null);
                };
            } else {
                err("Invalid response!");
            }
            // Begin accessing JSON data here
        };
        request.send();
        return this;
    };

    this.isConnected = function () {
        return _connected;
    };

    const _triggerEvent = function (key, data) {
        if (_events[key] == null) return;
        let i;
        for (i = 0; i < _events[key].length; i++) _events[key][i](data);
    };

    this.on = function (key, handler) {
        //check if event exists, if not, register
        if (_events[key] == null) _events[key] = [];
        _events[key].push(handler);
    }
}

//test
const client = new FireIoClient("localhost", 80);
client.setPassword("testpassword1");
client.connect();

client.on("connect", function () {
    console.log("connected!!");
});

client.on("disconnect", function () {
    console.log("disconnected!!");
});