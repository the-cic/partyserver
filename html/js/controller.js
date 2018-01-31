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
            $scope.onClickSelectChoice = function (value) {
                self.sendSelectChoice(value);
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
                    case "showChoice":
                        $scope.content.choice = message.body.choice;
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
                            var isShape = item.shape && true;
                            var isSprite = !item.background;
                            var isLabel = item.text && true;
                            var viewItem = {
                                id: item.id,
                                index: index,
                                shape: isShape ? item.shape : false,
                                text: isLabel ? item.text : false,
                                sprite: isSprite,
                                class: isShape
                                        ? (isSprite ? 'Sprite' : 'Background')
                                        : (isLabel ? 'Label' : ''),
                                style: isShape
                                        ? (isSprite ? {
                                            left: item.x + '%',
                                            top: item.y + '%',
                                            width: item.width + '%'
                                        } : {})
                                        : (isLabel ? {
                                            left: item.x + '%',
                                            top: item.y + '%',
                                        } : {})
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
                            if (viewItem) {
                                if (viewItem.shape) {
                                    if (viewItem.sprite) {
                                        viewItem.style = {
                                            left: item.x + '%',
                                            top: item.y + '%',
                                            width: viewItem.style.width
                                        };
                                    }
                                    if (item.shape) {
                                        viewItem.shape = item.shape;
                                    }
                                }
                                if (viewItem.text) {
                                    viewItem.style = {
                                        left: item.x + '%',
                                        top: item.y + '%'
                                    };
                                    if (item.text) {
                                        viewItem.text = item.text;
                                    }
                                }
                                self.viewBox.list[viewItem.index] = viewItem;
                            }
                        });
                        $scope.content.viewBox = self.viewBox.list;
                        break;
                    case "showJoystick":
                        $scope.content.joystick = {
                            '4': {n: 1, w: 1, e: 1, s: 1},
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
            self.sendSelectChoice = function (value) {
                if ($scope.content.disabled) {
                    return;
                }
                $scope.content.disabled = true;
                self.sendChoiceResponseMessage($scope.content.choice, value);
                $scope.content.choice = false;
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
            self.sendChoiceResponseMessage = function (choice, value) {
                console.log('value:' + value);
                var message = {
                    type: 'choice',
                    choice: {
                        id: choice.id,
                        value: value
                    }
                };
                DataService.send(JSON.stringify(message));
            };
            self.clearViewBox();
            ConnectController($scope, self);
        });

