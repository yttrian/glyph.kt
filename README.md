[![GitHub](https://img.shields.io/github/license/glyph-discord/glyph.kt)](https://github.com/glyph-discord/glyph.kt/blob/master/LICENSE)
[![GitHub Sponsors](https://img.shields.io/github/sponsors/yttrian)](https://github.com/sponsors/yttrian)

# glyph.kt

The Kotlin rewrite of the Glyph Discord bot.

Glyph is an experimental Discord bot that uses [DialogFlow](https://dialogflow.com/) to attempt to understand and
process natural language requests as opposed to a traditional command-based bot.

To learn more about how to use Glyph, check out the documentation [here](https://glyph.yttr.org/).

## Self-Hosting

Hosting your own copy of Glyph requires some setup.

1. Create and train a [DialogFlow](https://dialogflow.cloud.google.com/) agent that understands all actions Glyph's
   skills refer to
   - Actions are in the format "skill.feedback"
   - All referenced entities must be understood too
   - A free ("Trial") plan is enough
   - You should disable "Log interaction to Dialogflow"
2. Create a MariaDB database and Redis data store
3. Set the necessary environment variables as seen in the application.conf files
4. Build and start the bot and config website

## License

Glyph was previously licensed under the GNU Affero General Public License (AGPL)
and has now been re-licensed under the MIT License. 
See the [LICENSE.md](LICENSE.md) file for details.
