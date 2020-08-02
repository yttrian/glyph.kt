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
    this.invalidStateReason = document.getElementById("warning-reason");
    this.valid = true;

    var myself = this;

    this.load(this.populate);

    // Register button even handlers
    document.getElementById("save").onclick = function () {
        var config = myself.depopulate();

        myself.save(config, myself.setKey.bind(myself));
    };

    document.getElementById("load").onclick = function () {
        myself.load(myself.populate.bind(myself));
    };
};

/**
 * Warn the user that the config cannot be edited.
 * @param reason html reason to display
 */
Editor.prototype.invalidate = function (reason) {
    this.valid = false;
    document.body.classList.add("invalid");
    this.invalidStateReason.innerHTML = reason;
};

/**
 * Grabs the config key from the save bar input
 * @return {string} the config key
 */
Editor.prototype.getKey = function () {
    return this.keyInput.value;
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
 * Loads a config from a JSON string
 * @param {loadCallback} callback - function to call when request completes
 */
Editor.prototype.load = function (callback) {
    var myself = this;
    var xhr = new XMLHttpRequest();
    xhr.open("GET", myself.getKey() + "/data/");
    xhr.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                var responseJSON = JSON.parse(this.response);
                callback.call(myself, responseJSON);
            } else if (this.status === 401) {
                myself.invalidate("You are unauthorized to manage this server. Are you <a href='/'>logged</a> in?");
            } else if (this.status === 500) {
                myself.invalidate(this.response);
            }
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
 * Saves a config to a JSON string
 * @param {Object} config - the json config
 * @param {saveCallback} callback - function to call when request completes
 */
Editor.prototype.save = function (config, callback) {
    if (!this.valid) {
        // An invalid state can be caused by not being logged in, or trying to edit a server Glyph is not in
        // Either way, if the user was the still forcibly submit the form, they would be checked for permission first.
        // In the case of not being logged in, nothing would happen. In the case of saving to a non-Glyph server,
        // assuming they even have Manage Guild permissions, they would likely just end up messing up their config for
        // when they add Glyph and be upset at themselves.
        console.log("Refusing to save when in invalid state!");
        return;
    }
    var myself = this;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", myself.getKey() + "/data/");
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 201) {
            callback.call(myself, myself.getKey());
        } else if (this.readyState === 4 && this.status === 401) {
            myself.invalidate("You are unauthorized to manage this server. Are you <a href='/'>logged</a> in?");
        }
    };
    xhr.send(JSON.stringify(config));
};

var editor = new Editor();