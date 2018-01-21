'use strict';

angular.module('clientApp')
        .controller('ClientController', function ($scope, DataService, ConnectController) {
            var self = this;

            $scope.login = {
                nameInput: "name" + Math.round(Math.random() * 100),
                roomInput: ""
            };

            $scope.content = {
                disabled: true,
                assets: {},
                showAssets: false
            };

            $scope.onClickSendForm = function () {
                self.sendForm();
            };

            $scope.onPressJoystick = function (dx, dy) {
                self.pressJoystick(dx, dy);
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
                self.clearContent();
            };

            self.onSocketMessage = function (message) {
                switch (message.subject) {
                    case "command":
                        self.showCommand(message);
                        break;
                }
            };

            self.showCommand = function (message) {
                switch (message.body.action) {
                    case "clearView":
                        self.clearView();
                        break;
                    case "standBy":
                        $scope.content.standByText = message.body.text;
                        break;
                    case "showForm":
                        $scope.content.form = message.body.form;
                        $scope.content.disabled = false;
                        break;
                    case "storeAssets":
                        _.each(message.body.assets, function (value, key) {
                            $scope.content.assets[key] = value;
                        });
                        break;
                    case "showViewBox":
                        self.viewBox.list = [];
                        self.viewBox.items = {};
                        var index = 0;
                        _.each(message.body.items, function (item) {
                            var isSprite = !item.background;
                            var viewItem = {
                                id: item.id,
                                index: index,
                                shape: item.shape,
                                sprite: isSprite,
                                class: isSprite ? 'Sprite' : 'Background',
                                style: isSprite ? {
                                    left: item.x + '%',
                                    top: item.y + '%',
                                    width: item.width + '%'
                                } : {}
                            };
                            self.viewBox.list.push(viewItem);
                            self.viewBox.items[viewItem.id] = viewItem;
                            index++;
                        });
                        $scope.content.viewBox = self.viewBox.list;
                        break;
                    case "updateViewBox":
                        _.each(message.body.items, function (item) {
                            var viewItem = self.viewBox.items[item.id];
                            if (viewItem && viewItem.sprite) {
                                viewItem.style = {
                                    left: item.x + '%',
                                    top: item.y + '%',
                                    width: viewItem.style.width
                                };
                            }
                            self.viewBox.list[viewItem.index] = viewItem;
                        });
                        $scope.content.viewBox = self.viewBox.list;
                        break;
                    case "showJoystick":
                        $scope.content.joystick = {
                            '4': {nw: 1, n: 1, ne: 1, w: 1, e: 1, sw: 1, s: 1, se: 1},
                            '8': {nw: 1, n: 1, ne: 1, w: 1, e: 1, sw: 1, s: 1, se: 1}
                        }[message.body.directions];
                        $scope.content.disabled = false;
                        break;
                    case "hideJoystick":
                        $scope.content.joystick = false;
                        break;
                }
            };

            self.onSocketClose = function () {
                self.clearContent();
            };

            self.clearView = function () {
                $scope.content.standByText = false;
                $scope.content.form = false;
                $scope.content.joystick = false;
                $scope.content.viewBox = false;
            };

            self.clearContent = function () {
                $scope.content.standByText = false;
                $scope.content.form = false;
                $scope.content.assets = {};
                $scope.content.joystick = false;
                self.clearViewBox();
            };

            self.clearViewBox = function () {
                self.viewBox = {
                    list: [],
                    items: {}
                };
                $scope.content.viewBox = false;
            };

            self.sendForm = function () {
                if ($scope.content.disabled) {
                    return;
                }
                $scope.content.disabled = true;
                self.sendFormResponseMessage($scope.content.form);
                $scope.content.form = false;
            };

            self.pressJoystick = function (dx, dy) {
                if ($scope.content.disabled) {
                    return;
                }
                $scope.content.disabled = true;
                var message = {
                    type: 'joystick',
                    joystick: [dx, dy]
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendFormResponseMessage = function (form) {
                var values = {};
                _.each(form.fields, function (field) {
                    values[field.name] = field.value;
                });
                var message = {
                    type: 'form',
                    form: {
                        id: form.id,
                        values: values
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.clearViewBox();

            ConnectController($scope, self);

        });

