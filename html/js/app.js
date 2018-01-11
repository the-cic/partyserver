'use strict';

angular.module('clientApp', [])
        .filter('reverse', function () {
            return function (items) {
                if (!items) {
                    return items;
                }
                return items.slice().reverse();
            };
        })
        .filter('none', function () {
            return function (item, name) {
                if (!item || item === 'None') {
                    return 'No ' + name;
                }
                return item;
            };
        })
        .filter('timestamp', function () {
            return function (timestamp) {
                if (!timestamp) {
                    return timestamp;
                }
                return timestamp.replace(new RegExp('[a-zA-Z]', 'g'), ' ');
            };
        })
        .filter('words', function () {
            return function (str) {
                if (!str) {
                    return str;
                }
                // insert a space before all caps
                return str.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/_/g, ' ');
            };
        });

