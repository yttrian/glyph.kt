/*
 * populator.js
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2019 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Automatically populates and depopulates config options for easy editing
 * @param {string} dataTag - the data tag name to use for finding the config value
 * @constructor
 */
function Populator(dataTag) {
    console.log("Populator created");

    this.options = document.querySelectorAll("[data-" + dataTag + "]");
    this.handlers = {};

    // Add a default handler
    var defaultHandler = this.createHandler("default");

    defaultHandler.prototype.set = function (el, value) {
        el.value = value;
    };

    defaultHandler.prototype.get = function (el) {
        return el.value;
    };
}

/**
 * Register a handler for different data types
 * @param {string} key - the handler to name
 * @return {OptionHandler}
 */
Populator.prototype.createHandler = function (key) {
    /**
     * A handler for an option data type
     * @constructor
     */
    var OptionHandler = function () {
    };
    OptionHandler.prototype.set = function () {
    };
    OptionHandler.prototype.get = function () {
    };

    this.handlers[key] = new OptionHandler();

    console.log("Registered " + key + " handler");

    return OptionHandler;
};


/**
 * Retrieves the handler for getting and setting an inputs value
 * @param {Element} el - the targeted element
 * @return {OptionHandler}
 */
Populator.prototype.getHandler = function (el) {
    var explicitHandlerName = el.getAttribute("data-handler");
    var explicitHandler = this.handlers[explicitHandlerName];
    var inputType = el.getAttribute("type");
    var implicitHandler = this.handlers[inputType];

    if (explicitHandler !== undefined) {
        return explicitHandler;
    } else if (implicitHandler !== undefined) {
        return implicitHandler;
    }

    return this.handlers["default"];
};

/**
 * Populates the config editor using a config
 * @param {Object} config - the json config
 */
Populator.prototype.populate = function (config) {
    var myself = this;
    var data = config["_data"];

    this.options.forEach(function (el) {
        var selector = el.getAttribute("data-option").split(".");
        var handler = myself.getHandler(el);
        var value = myself.deepGet(config, selector);

        handler.set(el, value, data);
    });
};

/**
 * Depopulates the config editor
 * @return {Object} the json config
 */
Populator.prototype.depopulate = function () {
    var myself = this;
    var config = {};

    this.options.forEach(function (el) {
        var selector = el.getAttribute("data-option").split(".");
        var handler = myself.getHandler(el);

        var value = handler.get(el);

        myself.deepSet(config, selector, value);
    });

    return config;
};

/**
 * A recursive function to set a value deep within an object
 * @param {Object} object - the object to modify
 * @param {string[]} deepSelector - an array of selectors
 * @param {*} value - the value to set
 */
Populator.prototype.deepSet = function (object, deepSelector, value) {
    var currentSelector = deepSelector[0];
    var nextSelectors = deepSelector.slice(1);

    if (deepSelector.length > 1) {
        if (!object.hasOwnProperty(currentSelector)) {
            object[currentSelector] = {}
        }
        this.deepSet(object[currentSelector], nextSelectors, value);
    } else {
        object[currentSelector] = value;
    }
};

/**
 * A recursive function to get a value deep within an object
 * @param {Object} object - the object to dig into
 * @param {string[]} deepSelector - an array of selectors
 * @return {*} the retrieved value (if any)
 */
Populator.prototype.deepGet = function (object, deepSelector) {
    var currentSelector = deepSelector[0];
    var nextSelectors = deepSelector.slice(1);

    if (deepSelector.length > 1) {
        return this.deepGet(object[currentSelector], nextSelectors);
    } else {
        return object[currentSelector];
    }
};