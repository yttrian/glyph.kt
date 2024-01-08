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
const emoji = {
    "star": "⭐",
    "heart": "❤",
    "muscle": "💪",
    "fire": "🔥"
};
