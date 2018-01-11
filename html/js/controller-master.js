'use strict';

angular.module('clientApp')
        .controller('MasterController', function ($scope, $http, DataService, ConnectController) {
            var self = this;

            $scope.login = {
                nameInput: "name" + Math.round(Math.random() * 100),
                tokenInput: "405784574057349"
            };

            $scope.onClickTestSend = function () {
                self.sendTestForm();
            };

            $scope.onClickSendAssets = function () {
                self.sendAssets();
            };

            $scope.onClickSendViewBox = function () {
                self.sendViewBox();
            };

            $scope.onClickSendShowJoystick = function (directions) {
                self.sendShowJoystick(directions);
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
                    case "userResponse":
                        if (message.body.joystick) {
                            self.move(message.body.joystick);
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

            self.sendAssets = function () {
                var message = {
                    to: $scope.users,
                    body: {
                        action: "storeAssets",
                        assets: {
                            blob: self.assets.blob,
                            docking: self.assets.docking,
                            landscape: self.assets.landscape
                        }
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendViewBox = function () {
                var message = {
                    to: $scope.users,
                    body: {
                        action: "showViewBox",
                        items: [
                            {
                                id: 'landscape',
                                background: true
                            },
                            {
                                id: 'blob',
                                x: self.position.x,
                                y: self.position.y,
                                width: 20
                            },
                            {
                                id: 'docking',
                                x: 10 + Math.random() * 20,
                                y: 55 + Math.random() * 10,
                                width: 5
                            }
                        ]
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            self.sendShowJoystick = function (directions) {
                var message = {
                    to: $scope.users,
                    body: {
                        action: directions > 0 ? "showJoystick" : "hideJoystick",
                        directions: directions
                    }
                };
                DataService.send(JSON.stringify(message));
            };

            ConnectController($scope, self);

            self.assets = {};
            self.position = {
                x: 50,
                y: 15
            };

            self.move = function (direction) {
                var offset = 5;
                switch (direction) {
                    case 'nw':
                        self.position.x -= offset;
                        self.position.y -= offset;
                        break;
                    case 'n':
                        self.position.y -= offset;
                        break;
                    case 'ne':
                        self.position.x += offset;
                        self.position.y -= offset;
                        break;
                    case 'w':
                        self.position.x -= offset;
                        break;
                    case 'e':
                        self.position.x += offset;
                        break;
                    case 'sw':
                        self.position.x -= offset;
                        self.position.y += offset;
                        break;
                    case 's':
                        self.position.y += offset;
                        break;
                    case 'se':
                        self.position.x += offset;
                        self.position.y += offset;
                        break;
                }
                self.sendViewBox();
            };

            self.loadAsset = function (assetName, assetUrl) {
                $http.get(assetUrl, {responseType: 'blob'}).then(function (data) {
                    var reader = new FileReader();
                    reader.onloadend = function (e) {
                        console.log("Loaded asset:" + assetName);
                        self.assets[assetName] = e.target.result;
                    };
                    reader.readAsDataURL(data.data);
                });
            };

            self.loadAsset('blob', 'img/blob.jpg');
            self.loadAsset('docking', 'img/docking.png');
            self.loadAsset('landscape', 'img/landscape.jpg');

        });

