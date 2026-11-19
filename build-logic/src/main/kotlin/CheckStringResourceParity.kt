import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails when a locale's Compose string resources do not declare exactly the keys the default
 * `values/` directory declares.
 *
 * Compose resources fall back to `values/` without a word when a locale lacks a key, so a
 * half-translated screen builds, passes every test written against the default language, and
 * ships mixed. Android lint's `MissingTranslation` does not help: it reads Android `res/`, and
 * these strings live in `composeResources/`. A key that exists only in a locale is reported
 * too, since it is either a typo or a leftover of a string that was renamed.
 */
@CacheableTask
abstract class CheckStringResourceParity : DefaultTask() {

    /** The XML files of the module's `composeResources/values` directories, default and per locale. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stringFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check() {
        val keysByDirectory: Map<String, Set<String>> = stringFiles.files
            .groupBy { it.parentFile.name }
            .mapValues { (_, files) ->
                files.flatMap { file -> RESOURCE_NAME.findAll(file.readText()).map { it.groupValues[1] } }.toSet()
            }
        val defaultKeys = keysByDirectory[DEFAULT_DIRECTORY].orEmpty()

        val problems = keysByDirectory
            .filterKeys { it != DEFAULT_DIRECTORY }
            .toSortedMap()
            .flatMap { (directory, keys) ->
                (defaultKeys - keys).sorted().map { "$directory is missing \"$it\"" } +
                    (keys - defaultKeys).sorted().map { "$directory declares \"$it\", which $DEFAULT_DIRECTORY does not" }
            }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "String resources differ between locales:\n" + problems.joinToString("\n") { "  - $it" },
            )
        }
        report.get().asFile.writeText(
            "${defaultKeys.size} keys in ${keysByDirectory.keys.sorted().joinToString()}\n",
        )
    }

    private companion object {
        const val DEFAULT_DIRECTORY = "values"

        // <string>, <plurals> and <string-array> are the three kinds Compose resources read.
        val RESOURCE_NAME = Regex("""<(?:string|plurals|string-array)\s+name="([^"]+)"""")
    }
}
