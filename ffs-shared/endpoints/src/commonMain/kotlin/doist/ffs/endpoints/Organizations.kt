@file:Suppress("FunctionParameterNaming", "ConstructorParameterNaming", "FunctionNaming")

package doist.ffs.endpoints

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("organizations")
class Organizations {
    @Serializable
    @Resource("{id}")
    data class ById(val parent: Organizations = Organizations(), val id: Long) {

        @Serializable
        @Resource("users")
        data class Users(val parent: Organizations.ById) {

            @Serializable
            @Resource("{user_id}")
            data class ById(val parent: Users, val user_id: Long)
        }
    }

    companion object {
        const val NAME = "name"
        const val ROLE = "role"
        const val USER_ID = "user_id"

        fun ById.Users.Companion.ById(id: Long, user_id: Long) = ById.Users.ById(
            ById.Users(ById(id = id)),
            user_id = user_id
        )
    }
}
