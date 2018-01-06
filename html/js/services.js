'use strict';

angular.module('logApp')
        .factory('DataService', function () {
            var service = {
                listener: null
            };
            
            var connected = false;
            var connecting = false;
            var socket = false;

            service.connect = function () {
                if (connected || connecting) {
                    return false;
                }
                connecting = true;
                
                var hosts = ['localhost', '127.0.0.1'];
                var port = 8182;
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
                    service.listener.onSocketOpen();
                };

                socket.onclose = function () {
                    connected = false;
                    connecting = false;
                    socket = false;
                    service.listener.onSocketClose();
                };

                socket.onmessage = function (message) {
                    service.listener.onSocketMessage(JSON.parse(message.data));
                };
                
                return true;
            };
            
            service.send = function(message) {
                if (socket) {
                    socket.send(message);
                }
            };

            service.setListener = function (listener0) {
                service.listener = listener0;
            };

            return service;
        });

