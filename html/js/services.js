'use strict';

angular.module('clientApp')
        .factory('ConfigService', function ($http) {
            var service = {
                config: false,
                hostIndex: 0,
                getPort: function () {
                    return service.config.port;
                },
                resetHosts: function () {
                    console.log('reset hosts');
                    service.hostIndex = 0;
                },
                getNextHost: function () {
                    var host = false;
                    if (service.hasNextHost()) {
                        host = service.config.hosts[service.hostIndex];
                        service.hostIndex++;
                    }
                    return host;
                },
                hasNextHost: function () {
                    return service.config.hosts.length > service.hostIndex;
                }
            };

            console.log("Loading config");

            $http.get('config.json').then(function (res) {
                service.config = res.data;
                console.log("Config loaded");
            });

            return service;
        })
        .factory('DataService', function (ConfigService, $timeout) {
            var service = {
                listener: null
            };

            var connected = false;
            var connecting = false;
            var socket = false;
            var timeoutPromise = false;
            var reconnect = false;

            var onConnectionTimeout = function () {
                console.log('Connection timed out');
                if (socket) {
                    reconnect = ConfigService.hasNextHost();
                    console.log('Closing socket');
                    socket.close();
                }
            };

            service.canReconnect = function () {
                return reconnect;
            };

            service.connect = function () {
                if (!ConfigService.config) {
                    return;
                }
                if (connected || connecting) {
                    return false;
                }

                service.listener.onSocketConnecting();
                connecting = true;
                reconnect = false;
                timeoutPromise = false;

                var host = ConfigService.getNextHost();
                var port = ConfigService.getPort();
                var timeout = ConfigService.config.timeout;

                var path = 'ws://' + host + ':' + port;
                console.log('Connecting to ' + path);
                try {
                    timeoutPromise = $timeout(onConnectionTimeout, timeout, false);
                    socket = new WebSocket(path);
                } catch (e) {
                    console.error('===> WebSocket creation error :: ', e);
                }

                socket.addEventListener('open', function () {
                    console.log('Cancelling connection timeout');
                    $timeout.cancel(timeoutPromise);
                    connected = true;
                    connecting = false;
                    console.log('Connected');
                    service.listener.onSocketOpen();
                });

                socket.addEventListener('close', function (message) {
                    connected = false;
                    connecting = false;
                    socket = false;
                    console.log('Connection closed');
                    service.listener.onSocketClose(message.reason);
                });

                socket.addEventListener('message', function (message) {
                    service.listener.onSocketMessage(JSON.parse(message.data));
                });

                socket.addEventListener('error', function (error) {
                    console.log(error);
                });
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
        })
        .factory('ConnectController', function (DataService, ConfigService) {
            var $scope;
            var delegate;
            var controller = {
                foo: 'bar'
            };

            controller.onClickConnect = function () {
                delegate.onClickConnect();
                ConfigService.resetHosts();
                DataService.connect();
            };

            controller.onSocketConnecting = function () {
                $scope.disconnected = false;
                $scope.connectionError = false;
                $scope.connectionStatus = "Connecting...";
            };

            controller.onSocketOpen = function () {
                delegate.onSocketOpen();
                $scope.connectionStatus = "Connected";
                controller.log('Connected');
                $scope.$apply();
                var message = delegate.getLoginMessage();
                DataService.send(JSON.stringify(message));
            };

            controller.onSocketMessage = function (message) {
                controller.log(JSON.stringify(message));
                switch (message.subject) {
                    case "error":
                        if (message.body) {
                            $scope.connectionError = message.body.errorDescription;
                        }
                        break;
                    case "loginAccepted":
                        $scope.loggedIn = true;
                        break;
                }
                delegate.onSocketMessage(message);
                $scope.$apply();
            };

            controller.onSocketClose = function (reason) {
                $scope.disconnected = true;
                $scope.loggedIn = false;
                $scope.connectionStatus = "Disconnected";
                controller.log("Disconnected : " + reason);
                delegate.onSocketClose();
                if (DataService.canReconnect()) {
                    DataService.connect();
                }
                $scope.$apply();
            };

            controller.log = function (message) {
                if (message.length > 255) {
                    message = message.substring(0, 255) + '...';
                }
                $scope.log += "\n" + message;
            };

            DataService.setListener(controller);

            return function (scope, self) {
                $scope = scope;
                delegate = self;

                $scope.disconnected = true;
                $scope.loggedIn = false;
                $scope.connectionStatus = "Disconnected";
                $scope.connectionError = false;
                $scope.log = "";

                $scope.onClickConnect = function () {
                    controller.onClickConnect();
                };

                return controller;
            };
        });

