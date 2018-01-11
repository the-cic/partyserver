'use strict';

angular.module('clientApp')
        .controller('MasterController', function ($scope, DataService, ConnectController) {
            var self = this;

            $scope.login = {
                nameInput: "name" + Math.round(Math.random() * 100),
                tokenInput: "405784574057349"
            };

            $scope.onClickTestSend = function () {
                self.sendTestForm();
            };

            self.onClickConnect = function () {
                self.name = $scope.login.nameInput;
                self.token = $scope.login.tokenInput;
                $scope.users = [];
            };

            self.getLoginMessage = function () {
                return {
                    login: self.name,
                    token: self.token
                };
            };

            self.onSocketOpen = function () {};

            self.onSocketMessage = function (message) {
                switch (message.subject) {
                    case "roomCreated":
                        $scope.roomCode = message.body.name;
                        break;
                    case "userConnected":
                        $scope.users.push(message.body.name);
                        self.sendWaitMessage(message.body.name, "Hello " + message.body.name + ", please wait a while.");
                        break;
                    case "userDisconnected":
                        var userIndex = $scope.users.indexOf(message.body.name);
                        if (userIndex > -1) {
                            $scope.users.splice(userIndex, 1);
                        }
                        break;
                }
            };

            self.onSocketClose = function () {};

            self.sendTestForm = function () {
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

            self.sendWaitMessage = function (user, text) {
                var message = {
                    to: [user],
                    body: {
                        action: "standBy",
                        text: text
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendShowFormMessage = function (form) {
                var message = {
                    to: $scope.users,
                    body: {
                        action: "showForm",
                        form: form
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            ConnectController($scope, self);

        });

