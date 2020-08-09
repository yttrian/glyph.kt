/*
 * emoji.js
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
 * Emoji and their shortnames for the emoji picker
 *
 * Instead of dealing with tons of possible emoji and variants,
 * we'll just limit it to a select few for no good reason other
 * than the fact that it makes it easier to make sure they all work
 * properly. We also don't have to worry about twemoji having a heart
 * attack when told emoji like :cheese: which it doesn't seem to think
 * exists for some reason. It also seems to not be happy with skin tones
 * they way I did it before, so I'll just keep things simple.
 *
 * @type {Object.<string, string>}
 */
var emoji = {
    "star": "⭐",
    "heart": "❤"
};