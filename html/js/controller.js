'use strict';

angular.module('logApp')
        .controller('MainController', function ($scope, DataService) {
            var self = this;

            DataService.setListener(self);
            $scope.disconnected = true;
            $scope.loggedIn = false;
            $scope.isMaster = false;
            $scope.connectionStatus = "Disconnected";
            $scope.connectionError = false;
            $scope.updateCount = 0;
            $scope.subNavigation = 0;
            $scope.roomInput = "room1";
            $scope.nameInput = "name" + Math.round(Math.random() * 100);
            $scope.masterInput = false;
            $scope.tokenInput = "405784574057349";
            $scope.log = "";
            $scope.content = {
                action: ""
            };

            $scope.onClickConnect = function () {
                $scope.disconnected = false;
                $scope.connectionError = false;
                self.code = $scope.roomInput;
                self.name = $scope.nameInput;
                self.master = $scope.masterInput;
                self.token = $scope.tokenInput;
                $scope.isMaster = self.master;
                if (DataService.connect()) {
                    $scope.connectionStatus = "Connecting...";
                }
            };

            $scope.onClickSendForm = function () {
                self.sendResponseMessage($scope.content.form);
                $scope.content.action = "standBy";
                $scope.content.standByText = "Response sent";
            };

            $scope.onClickTestSend = function () {
                var form = {
                    title: "Type some things",
                    id: "form1",
                    fields: [
                        {
                            type: "text",
                            name: "reply 1"
                        },
                        {
                            type: "text",
                            name: "reply 2"
                        }
                    ]
                };
                self.sendShowFormMessage(form);
            };

            self.onSocketOpen = function () {
                $scope.connectionStatus = "Connected";
                $scope.log += "\n" + "Connected";
                $scope.content.action = "standBy";
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

            self.onSocketMessage = function (message) {
                $scope.updateCount++;
                $scope.log += "\n" + JSON.stringify(message);
                if (message.error) {
                    $scope.connectionError = message.error;
                }
                switch (message.subject) {
                    case "loginAccepted":
                        $scope.loggedIn = true;
                        break;
                    case "roomCreated":
                        $scope.content.roomCode = message.body.name;
                        break;
                    case "userConnected":
                        self.sendWaitMessage(message.body.name, "Hello " + message.body.name + ", please wait a while.");
                        break;
                    case "standBy":
                        $scope.content.action = "standBy";
                        $scope.content.standByText = message.body.text;
                        break;
                    case "showForm":
                        $scope.content.action = "showForm";
                        $scope.content.form = message.body;
                        break;
                }
                $scope.$apply();
            };

            self.onSocketClose = function () {
                $scope.disconnected = true;
                $scope.loggedIn = false;
                $scope.connectionStatus = "Disconnected";
                $scope.log += "\n" + "Disconnected";
                $scope.content.action = "";
                $scope.$apply();
            };

            self.sendWaitMessage = function (user, text) {
                var message = {
                    target: user,
                    subject: "standBy",
                    body: {
                        text: text
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendResponseMessage = function (form) {
                var values = {};
                _.each(form.fields, function (field) {
                    values[field.name] = field.value;
                });
                var message = {
                    target: "",
                    subject: "formResponse",
                    body: {
                        id: form.id,
                        values: values
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendShowFormMessage = function (form) {
                var message = {
                    target: "*",
                    subject: "showForm",
                    body: form
                };
                DataService.send(JSON.stringify(message));
            };

        });

