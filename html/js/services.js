'use strict';

angular.module('clientApp')
        .factory('ConfigService', function ($http) {
            var service = {
                config: false
            };

            console.log("Loading config");
            
            $http.get('config.json').then(function (res) {
                service.config = res.data;
                console.log("Config loaded");
            });
            
            return service;
        })
        .factory('DataService', function (ConfigService) {
            var service = {
                listener: null
            };

            var connected = false;
            var connecting = false;
            var socket = false;

            service.connect = function () {
                if (!ConfigService.config) {
                    return;
                }
                if (connected || connecting) {
                    return false;
                }
                connecting = true;

                var hosts = ConfigService.config.hosts;
                var port = ConfigService.config.port;
                for (var i in hosts) {
                    var path = 'ws://' + hosts[i] + ':' + port;
                    console.log('Connecting to ' + path);
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
                    console.log('Connected');
                    service.listener.onSocketOpen();
                };

                socket.onclose = function (message) {
                    connected = false;
                    connecting = false;
                    socket = false;
                    console.log('Connection closed');
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
        })
        .factory('ConnectController', function (DataService) {
            var $scope;
            var delegate;
            var controller = {
                foo: 'bar'
            };

            controller.onClickConnect = function () {
                delegate.onClickConnect();
                $scope.disconnected = false;
                $scope.connectionError = false;
                if (DataService.connect()) {
                    $scope.connectionStatus = "Connecting...";
                }
            };

            controller.onSocketOpen = function () {
                delegate.onSocketOpen();
                $scope.connectionStatus = "Connected";
                $scope.log += "\n" + "Connected";
                $scope.$apply();
                var message = delegate.getLoginMessage();
                DataService.send(JSON.stringify(message));
            };

            controller.onSocketMessage = function (message) {
                $scope.log += "\n" + JSON.stringify(message);
                if (message.error) {
                    $scope.connectionError = message.error;
                }
                switch (message.subject) {
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
                $scope.log += "\n" + "Disconnected : " + reason;
                delegate.onSocketClose();
                $scope.$apply();
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

