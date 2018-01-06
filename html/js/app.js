'use strict';

angular.module('logApp', [])
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
        })
        .filter('reverse', function () {
            return function (items) {
                if (!items) {
                    return items;
                }
                return items.slice().reverse();
            };
        })
        .filter('none', function () {
            return function (item, name) {
                if (!item || item === 'None') {
                    return 'No ' + name;
                }
                return item;
            };
        })
        .filter('timestamp', function () {
            return function (timestamp) {
                if (!timestamp) {
                    return timestamp;
                }
                return timestamp.replace(new RegExp('[a-zA-Z]', 'g'), ' ');
            };
        })
        .filter('words', function () {
            return function (str) {
                if (!str) {
                    return str;
                }
                // insert a space before all caps
                return str.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/_/g, ' ');
            };
        });

