'use strict';

angular.module('clientApp')
        .controller('ClientController', function ($scope, DataService, ConnectController) {
            var self = this;

            $scope.login = {
                nameInput: "name" + Math.round(Math.random() * 100),
                roomInput: "room1"
            };

            $scope.content = {
                action: "",
                assets: {}
            };

            $scope.onClickSendForm = function () {
                self.sendForm();
            };

            self.onClickConnect = function () {
                self.room = $scope.login.roomInput;
                self.name = $scope.login.nameInput;
            };

            self.getLoginMessage = function () {
                return {
                    login: self.name,
                    room: self.room
                };
            };

            self.onSocketOpen = function () {
                $scope.content.action = "standBy";
            };

            self.onSocketMessage = function (message) {
                switch (message.subject) {
                    case "command":
                        self.showCommand(message);
                        break;
                }
            };

            self.showCommand = function (message) {
                $scope.content.action = message.body.action;
                switch (message.body.action) {
                    case "standBy":
                        $scope.content.standByText = message.body.text;
                        break;
                    case "showForm":
                        $scope.content.form = message.body.form;
                        break;
                    case "storeAssets":
                        message.body.assets;
                        _.each(message.body.assets, function (value, key) {
                            $scope.content.assets[key] = value;
                        });
                        break;
                }
            };

            self.onSocketClose = function () {
                $scope.content.action = "";
                $scope.content.assets = {};
            };

            self.sendForm = function () {
                self.sendResponseMessage($scope.content.form);
                $scope.content.action = "standBy";
                $scope.content.standByText = "Response sent";
            };

            self.sendResponseMessage = function (form) {
                var values = {};
                _.each(form.fields, function (field) {
                    values[field.name] = field.value;
                });
                var message = {
                    body: {
                        id: form.id,
                        values: values
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            ConnectController($scope, self);

        });

