'use strict';

angular.module('logApp')
        .controller('MainController', function ($scope, DataService) {
            var self = this;

            DataService.setListener(self);
            $scope.connectionStatus = "Disconnected";
            $scope.updateCount = 0;
            $scope.subNavigation = 0;
            $scope.roomInput = "room1";
            $scope.nameInput = "name";
            $scope.masterInput = false;
            $scope.tokenInput = "";
            $scope.log = "";

            $scope.onClickConnect = function () {
                self.code = $scope.roomInput;
                self.name = $scope.nameInput;
                self.master = $scope.masterInput;
                self.token = $scope.tokenInput;
                if (DataService.connect()) {
                    $scope.connectionStatus = "Connecting...";
                }
            };

            $scope.onClickSend = function () {
                var message = {
                    target : self.master ? "*" : "",
                    subject : "hello",
                    body : {
                        text : "hello I am " + self.name
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            this.onSocketOpen = function () {
                $scope.connectionStatus = "Connected";
                $scope.log += "\n" + "Connected";
                $scope.$apply();
                var message = {
                    room: self.code,
                    login: self.name
                };
                if (self.master) {
                    message.room = null;
                    message.token = self.token;
                }
                DataService.send(JSON.stringify(message));
            };

            this.onSocketMessage = function (message) {
                $scope.updateCount++;
                $scope.log += "\n" + JSON.stringify(message);
                $scope.$apply();
            };

            this.onSocketClose = function () {
                $scope.connectionStatus = "Disconnected";
                $scope.log += "\n" + "Disconnected";
                $scope.$apply();
            };

        });

