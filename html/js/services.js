'use strict';

angular.module('logApp')
        .factory('DataService', function ($http) {
            var service = {
                listener: null
            };

            var connected = false;
            var connecting = false;
            var socket = false;
            var config = {};

            $http.get('config.json').then(function (res) {
                config = res.data;
                console.log("config loaded");
                console.log(config);
            });

            service.connect = function () {
                if (connected || connecting) {
                    return false;
                }
                connecting = true;

                var hosts = config.hosts;
                var port = config.port;
                for (var i in hosts) {
                    var path = 'ws://' + hosts[i] + ':' + port;
                    console.log('connecting to ' + path);
                    try {
                        socket = new WebSocket(path);
                        break;
                    } catch (e) {
                        console.error('===> WebSocket creation error :: ', e);
                    }
                }

                socket.onopen = function () {
                    connected = true;
                    connecting = false;
                    console.log('connected');
                    service.listener.onSocketOpen();
                };

                socket.onclose = function (message) {
                    connected = false;
                    connecting = false;
                    socket = false;
                    console.log('connection closed');
                    service.listener.onSocketClose(message.reason);
                };

                socket.onmessage = function (message) {
                    service.listener.onSocketMessage(JSON.parse(message.data));
                };

                return true;
            };

            service.send = function (message) {
                if (socket) {
                    socket.send(message);
                }
            };

            service.setListener = function (listener0) {
                service.listener = listener0;
            };

            return service;
        });

