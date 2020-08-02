/*
 * editor.js
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
 * The main config editor logic
 * @constructor
 */
function Editor() {
    this.init();
}

/**
 * Initializes the config editor
 */
Editor.prototype.init = function () {
    console.log("Editor created");

    this.keyInput = document.getElementById("key");
    this.populator = new Populator("option");
    this.hinter = new Hinter("hint", "hinter");

    var myself = this;

    this.load(this.populate);

    // Register button even handlers
    document.getElementById("save").onclick = function () {
        var config = myself.depopulate();

        myself.save(config, myself.setKey.bind(myself))
    };

    document.getElementById("load").onclick = function () {
        myself.load(myself.populate.bind(myself));
    };
};

/**
 * Grabs the config key from the save bar input
 * @return {string} the config key
 */
Editor.prototype.getKey = function () {
    return this.keyInput.value
};

/**
 * Sets the config key in the save bar input
 * @param {string} key - the config key
 */
Editor.prototype.setKey = function (key) {
    this.keyInput.value = key;
};

/**
 * Defers to the populator to add a data type handler
 * @param {string} key - the handler name
 * @return {OptionHandler}
 */
Editor.prototype.createHandler = function (key) {
    return this.populator.createHandler(key);
};

/**
 * Defers to the populator to populate the config editor
 * @param {Object} config - the json config
 */
Editor.prototype.populate = function (config) {
    if (!config.hasOwnProperty("_data")) return;

    this.populator.populate(config);
};

/**
 * Defers to the populator to depopulate the config editor
 * @return {Object} the json config
 */
Editor.prototype.depopulate = function () {
    return this.populator.depopulate();
};

/**
 * A callback to run when a config is loaded
 * @callback loadCallback
 * @param {Object} config
 */

/**
 * Loads a config from a MicroConfig string
 * @param {loadCallback} callback - function to call when request completes
 */
Editor.prototype.load = function (callback) {
    var myself = this;
    var xhr = new XMLHttpRequest();
    xhr.open("GET", myself.getKey() + "/data/");
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            var responseJSON = JSON.parse(this.response);

            callback.call(myself, responseJSON);
        } else if (this.readyState === 4 && this.status === 401) {
            document.location = "/";
        }
    };
    xhr.send();
};

/**
 * A callback to run when a config is saved
 * @callback saveCallback
 * @param {string} key
 */

/**
 * Saves a config to a MicroConfig string
 * @param {Object} config - the json config
 * @param {saveCallback} callback - function to call when request completes
 */
Editor.prototype.save = function (config, callback) {
    var myself = this;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", myself.getKey() + "/data/");
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 201) {
            callback.call(myself, myself.getKey());
        }
    };
    xhr.send(JSON.stringify(config));
};

var editor = new Editor();