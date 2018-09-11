/*
 * JavaScript client for the Fire-IO server
 *
 * The Fire-IO server does not use the WebSocket protocol by default but it will switch and accept
 * once a WebSocket http header is first received.
 *
 * It will translate packets on the server side for use with the Java API.
 *
 * GitHub: https://github.com/Mindgamesnl/FireIO
 */

function FireIoClient(host, port) {

    // variables
    const _host = host;
    const _port = port;
    const _events = {};
    let _reconnectTimer = -1;
    let _password = "";
    let _ws;
    let _token;
    let _connected = false;

    // error toggle
    const err = function(err) {
        console.error("[Fire-IO] " + err)
    };

    // set password
    this.setPassword = function(npass) {
        _password = npass;
        return this;
    };

    //api connect
    this.connect = function () {
        directConnect(_host, _port);
    };

    //connect
    let directConnect = function(ihost, iport) {
        // setup http client
        const request = new XMLHttpRequest();
        // request the token with optional password
        // this is the same endpoint as the java client
        request.open('GET', 'http://' + ihost + ":" + iport + "/fireio/register?p=" + _password, true);

        // handle the data returned
        request.onload = function(a) {
            // get only the request string
            const response = a.target.responseText;

            // check the response types
            // is the response rate limit? then its blocked by the rate limiter (wow)
            if (response === "ratelimit") {
                err("The server denied the request due to the rate limiter. please retry later.");
                return;
            }
            // is the response fail-auth? then the password is either invalid or empty, in short, ya can't get in kiddo
            if (response === "fail-auth") {
                err("The server denied the request due to a authentication problem. is your password correct?");
                return;
            }

            if (response.startsWith("redirect=")) {
                let res = response.replace("redirect=", "");
                let redirected = res.split("INFO:")[0].split(":");
                let rehost = redirected[0];
                let report = parseInt(redirected[1]);
                err("Connected to loadbalancer. Re-directing to next avalible server in pool.");
                _recondirfunc(rehost, report);
                return;
            }

            // check if the response is longer than 8, cuz that means it succeeded
            if (response.length > 8) {
                //ok, try to get the token to use
                _token = response.split("INFO:")[0];
                //setup WebSocket
                _ws = new WebSocket("ws://" + ihost + ":" + (iport + 1) + "/" + _token);

                // setup WebSocket events
                // on close
                _ws.onclose = function() {
                    _connected = false;
                    _triggerEvent("close", null);
                    _triggerEvent("disconnect", null);
                };

                // on error
                _ws.onerror = function(onclose) {
                    console.log(onclose);
                };

                // on message, received data from the server, handle it like you shoeld
                _ws.onmessage = function(message) {
                    let input = message.data;
                    if (input.startsWith("channelmessage:")) {
                        //it is a channel message
                        input = input.replace("channelmessage:", "");
                        let channel = input.split(":")[0];
                        input = input.replace(channel + ":", "");
                        _triggerEvent(channel, input);
                    }
                };

                // on open
                _ws.onopen = function() {
                    _connected = true;
                    _triggerEvent("connect", null);
                    _triggerEvent("open", null);
                };
            } else {
                //shorter than 8 but a unknown value, is this client up-to-date?
                err("Invalid response!");
            }
        };

        // finish and make the request
        request.send();
        return this;
    };

    // a static refference to the connect function for internal use
    const _reconfunc = this.connect;
    const _recondirfunc = directConnect;

    // a simple api check to see if the client is connected or not
    this.isConnected = function() {
        return _connected;
    };

    // send a string channel and string fire, this will trigger the ChannelMessagePacket on the Fire-IO server
    // this triggers a event on the server with the channel name as channel and a ReceivedText event payload
    this.send = function(channel, data) {
        _ws.send("channelmessage:" + channel + ":" + data);
    };

    // check if event exists by key with handler(s)
    // if it does, loop for all handlers and then execute them with the data
    const _triggerEvent = function(key, data) {
        if (_events[key] == null) return;
        let i;
        for (i = 0; i < _events[key].length; i++) _events[key][i](data);
    };

    // register listener, with function name (String) and handler (Function) that takes the data as argument
    this.on = function(key, handler) {
        //check if event exists, if not, register
        if (_events[key] == null) _events[key] = [];
        _events[key].push(handler);
    };

    // auto re connect, takes milliseconds as argument for the time out
    // this sets a loop that's started by on disconnect that attempts to reconnect periodically, and it stops the loop when a connection is astonished again
    this.setAutoReconnect = function(timeout) {
        // register on disconnect event
        client.on("disconnect", function() {
            console.log("[Fire-IO] Attempting re-connect every " + timeout + "MS");
            // start timer and save id
            _reconnectTimer = setInterval(function() {
                _reconfunc();
            }, timeout);
        });

        // listen for when a connection comes back alive
        client.on("connect", function() {
            // cancel the interval if set
            if (_reconnectTimer != -1) clearInterval(_reconnectTimer);
        });
    }
}