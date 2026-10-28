# WorldBattles Nametags

Forge client mod for Minecraft 1.20.1.

## Behavior
- Cancels player name-tag rendering while the local player is in `minecraft:overworld`.
- Leaves player name tags unchanged in Freelands, Minage, the Nether, and the End.
- Designed as a base for future role/grade labels in Freelands.

## Build
GitHub Actions compiles the project with Java 17. Open the **Actions** tab, select **Build Forge mod**, and download the `WorldBattlesNametags-Forge-1.20.1` artifact after a successful run.

Install the resulting JAR in the `mods` folder of every player's Forge 1.20.1 client. This is a client-side rendering mod; installing it only on the server is not sufficient.
