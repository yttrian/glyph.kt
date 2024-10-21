[![GitHub](https://img.shields.io/github/license/glyph-discord/glyph.kt)](https://github.com/glyph-discord/glyph.kt/blob/master/LICENSE)
[![GitHub Sponsors](https://img.shields.io/github/sponsors/yttrian)](https://github.com/sponsors/yttrian)
[![Discord Bots](https://discordbots.org/api/widget/status/248186527161516032.svg?noavatar=true)](https://discordbots.org/bot/248186527161516032)
[![Discord Bots](https://discordbots.org/api/widget/servers/248186527161516032.svg?noavatar=true)](https://discordbots.org/bot/248186527161516032)

# glyph.kt

The Kotlin rewrite of the Glyph Discord bot.

Glyph is an experimental Discord bot that uses [DialogFlow](https://dialogflow.com/) to attempt to understand and
process natural language requests as opposed to a traditional command based bot.

To learn more about how to use Glyph, check out the documentation [here](https://gl.yttr.org/).

## Self Hosting

In order to host your own copy of Glyph, some set up will be required.

1. Create and train a [DialogFlow](https://dialogflow.cloud.google.com/) agent that understands all actions Glyph's
   skills refer to
   - Action is in the format "skill.feedback"
   - All references entities must be understood too
   - A free ("Trial") plan is sufficient
   - You should disable "Log interaction to Dialogflow"
2. Create a PostgreSQL database and Redis data store
3. Set the needed environment variables as seen in the application.conf files
4. Build and start the bot and config website

## License

Glyph was previously licensed under the GNU Affero General Public License (AGPL)
and has now been re-licensed under the MIT License. 
See the [LICENSE.md](LICENSE.md) file for details.
