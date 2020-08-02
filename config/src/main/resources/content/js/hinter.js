/*
 * hinter.js
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
 * Displays hints in the save bar to help explain things
 * @param dataTag the data tag to select
 * @param displayID the element id for where to show the hint
 * @constructor
 */
function Hinter(dataTag, displayID) {
    this.hinted = document.querySelectorAll("[data-" + dataTag + "]");
    this.hintDisplay = document.getElementById(displayID);

    var myself = this;

    this.hinted.forEach(function (el) {
        el.onmouseover = function () {
            myself.hintDisplay.innerText = this.getAttribute("data-" + dataTag);
        };

        el.onmouseleave = function () {
            myself.hintDisplay.innerText = "";
        }
    });
}

