package buildSrc.tasks.remapping

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.annotations.Expose
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.gradle.api.JavaVersion
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories

class MixinJson(
    @field:Expose var required: Boolean,
    @field:Expose var `package`: String,
    @field:Expose var compatabilityLevel: JavaVersion,
    @field:Expose var minVersion: String,
    @field:Expose val mixins: MutableList<String> = mutableListOf(),
    @field:Expose val client: MutableList<String> = mutableListOf(),
    @field:Expose val server: MutableList<String> = mutableListOf(),
) {
    fun isEmpty() = mixins.isEmpty() &&
            client.isEmpty() &&
            server.isEmpty()

    fun optional(): MixinJson {
        return MixinJson(
            false,
            `package`,
            compatabilityLevel,
            minVersion,
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )
    }

    fun addMixin(env: MixinEnv, name: String) {
        when (env) {
            MixinEnv.COMMON -> mixins.add(name)
            MixinEnv.CLIENT -> client.add(name)
            MixinEnv.SERVER -> server.add(name)
        }
    }

    fun writeTo(path: Path) {
        path.parent.createDirectories()
        path.bufferedWriter(
            options = arrayOf(
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        ).use {
            GSON.toJson(this, it)
        }
    }
}

public class JavaVersionAdapter : TypeAdapter<JavaVersion>() {
    override fun write(out: JsonWriter, value: JavaVersion) {
        out.value(value.name.replace("VERSION", "JAVA"))
    }

    override fun read(`in`: JsonReader): JavaVersion {
        return JavaVersion.valueOf(`in`.nextString().replace("JAVA", "VERSION"))
    }
}
private val GSON = GsonBuilder()
    .registerTypeAdapter(JavaVersion::class.java, JavaVersionAdapter())
    .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
    .excludeFieldsWithoutExposeAnnotation()
    .disableHtmlEscaping()
    .setPrettyPrinting()
    .setStrictness(Strictness.LENIENT)
    .create()

private class JavaVersion
