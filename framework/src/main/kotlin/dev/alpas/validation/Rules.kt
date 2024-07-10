package dev.alpas.validation

import dev.alpas.http.HttpCall
import org.hazlewood.connor.bottema.emailaddress.EmailAddressCriteria
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator

typealias ErrorMessage = ((String, Any?) -> String)?

open class Max(private val length: Int, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return ((value ?: "").toString().length <= length).also {
            if (!it) {
                error =
                    message?.let { it(attribute, value) } ?: "The '$attribute' must be at most $length characters long."
            }
        }
    }
}

open class Min(private val length: Int, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return ((value ?: "").toString().length >= length).also {
            if (!it) {
                error =
                    message?.let { it(attribute, value) } ?: "The '$attribute' must be at least $length characters long."
            }
        }
    }
}

// Attribute must be present and the value must not be null or empty.
open class Required(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        if (value as? List<Any?> != null) {
            val passes = value.isNotEmpty()
            if (!passes) {
                error = message?.let { it(attribute, value) }
                    ?: "The required field '$attribute' is empty."
            }
            return passes
        }
        val passes = !value?.toString().isNullOrBlank()
        if (!passes) {
            error = message?.let { it(attribute, value) }
                ?: "The required field '$attribute' is missing, null, or empty."
        }
        return passes
    }
}

// Attribute must be present and the value must not be null. It can be empty.
open class NotNull(private val message: ErrorMessage = null) : Rule() {
    @Suppress("KotlinConstantConditions")
    override fun check(attribute: String, value: Any?): Boolean {
        return (value != null).also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The non null field '$attribute' is null."
            }
        }
    }
}

open class MustBeInteger(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        val valueStr = value?.toString()
        val isValid = valueStr?.toIntOrNull() ?: valueStr?.toLongOrNull() != null
        return isValid.also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The field '$attribute' must be an integer."
            }
        }
    }
}

open class MustBeString(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        val isValid = value is String
        return isValid.also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The field '$attribute' must be a string."
            }
        }
    }
}

open class Email(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (!value?.toString().isNullOrBlank() && EmailAddressValidator.isValid(
            value?.toString(),
            EmailAddressCriteria.RFC_COMPLIANT
        )).also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "'$attribute' is not a valid email address."
            }
        }
    }
}

open class MatchesRegularExpression(private val expression: String, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (value?.toString()?.matches(expression.toRegex()) == true).also {
            if (!it) {
                error =
                    message?.let { it(attribute, value) } ?: "The field '$attribute' did not match the required format."
            }
        }
    }
}

open class Confirm(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        val confirmAttribute1 = "${attribute}_confirm"
        val confirmAttribute2 = "confirm_$attribute"
        val value = call.param(attribute)
        val valueConfirm = call.param(confirmAttribute1) ?: call.param(confirmAttribute2)
        return (!valueConfirm?.toString().isNullOrBlank() && value == valueConfirm).also {
            if (!it) {
                error = message?.let { it(attribute, valueConfirm) } ?: "The '$attribute' confirmation does not match."
            }
        }
    }
}

open class JsonField(private val rules: List<Rule>) : Rule() {
    constructor(rule: Rule, vararg rules: Rule) : this(listOf(rule, *rules))

    override fun check(attribute: String, call: HttpCall): Boolean {
        if (!call.isJson || call.jsonBody == null) {
            error = "Call isn't a JSON or the body isn't a valid JSON."
            return false
        }
        val value = call.jsonBody[attribute]
        rules.forEach { rule ->
            if (!rule.check(attribute, value)) {
                error = rule.error
                return false
            }
        }
        return true
    }
}

// Attribute is required if other attribute exists
open class RequiredIf(private val otherAttribute: String, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        if (call.filled(otherAttribute)) {
            val value = call.param(attribute)
            val passes = call.filled(attribute)
            if (!passes) {
                error =
                    message?.let { it(attribute, value) }
                        ?: "$attribute cannot be null or empty when $otherAttribute is present."
            }
            return passes
        }
        return true
    }
}

fun ValidationGuard.max(length: Int, message: ErrorMessage = null): Rule {
    return rule(Max(length, message))
}

fun ValidationGuard.min(length: Int, message: ErrorMessage = null): Rule {
    return rule(Min(length, message))
}

fun ValidationGuard.required(message: ErrorMessage = null): Rule {
    return rule(Required(message))
}

fun ValidationGuard.notNull(message: ErrorMessage = null): Rule {
    return rule(NotNull(message))
}

fun ValidationGuard.confirm(message: ErrorMessage = null): Rule {
    return rule(Confirm(message))
}

fun ValidationGuard.email(message: ErrorMessage = null): Rule {
    return rule(Email(message))
}

fun ValidationGuard.mustBeInteger(message: ErrorMessage = null): Rule {
    return rule(MustBeInteger(message))
}

fun ValidationGuard.mustBeString(message: ErrorMessage = null): Rule {
    return rule(MustBeString(message))
}

fun ValidationGuard.matchesRegularExpression(expression: String, message: ErrorMessage = null): Rule {
    return rule(MatchesRegularExpression(expression, message))
}

fun ValidationGuard.jsonField(rules: List<Rule>): Rule {
    return rule(JsonField(rules))
}

fun ValidationGuard.inJsonBody(rules: ValidationGuard.() -> Rule) {
    inJsonBodyContext = true
    rules()
    inJsonBodyContext = false
}

fun ValidationGuard.requiredIf(otherAttribute: String, message: ErrorMessage = null): Rule {
    return rule(RequiredIf(otherAttribute, message))
}
