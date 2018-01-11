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

            $scope.onPressJoystick = function (direction) {
                self.pressJoystick(direction);
            };

            self.onClickConnect = function () {
                self.room = $scope.login.roomInput.toUpperCase();
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
                        _.each(message.body.assets, function (value, key) {
                            $scope.content.assets[key] = value;
                        });
                        break;
                    case "showViewBox":
                        var viewBox = [];
                        _.each(message.body.items, function (item) {
                            var viewItem = {
                                id: item.id,
                                class: item.background ? 'Background' : 'Sprite',
                                style: item.background ? {} : {
                                    left: item.x + '%',
                                    top: item.y + '%',
                                    width: item.width + '%'
                                }
                            };
                            viewBox.push(viewItem);
                        });
                        $scope.content.viewBox = viewBox;
                        break;
                    case "showJoystick":
                        $scope.content.joystick = {
                            '4': {n: 1, s: 1, e: 1, w: 1},
                            '8': {nw: 1, n: 1, ne: 1, w: 1, e: 1, sw: 1, s: 1, se: 1}
                        }[message.body.directions];
                        break;
                    case "hideJoystick":
                        $scope.content.joystick = false;
                        break;
                }
            };

            self.onSocketClose = function () {
                $scope.content.action = "";
                $scope.content.assets = {};
                $scope.content.viewBox = false;
                $scope.content.joystick = false;
            };

            self.sendForm = function () {
                self.sendFormResponseMessage($scope.content.form);
                $scope.content.action = "standBy";
                $scope.content.standByText = "Response sent";
            };

            self.pressJoystick = function (direction) {
                var message = {
                    joystick: direction
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendFormResponseMessage = function (form) {
                var values = {};
                _.each(form.fields, function (field) {
                    values[field.name] = field.value;
                });
                var message = {
                    form: {
                        id: form.id,
                        values: values
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            ConnectController($scope, self);

        });

