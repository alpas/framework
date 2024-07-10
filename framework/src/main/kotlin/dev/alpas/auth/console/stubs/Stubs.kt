package dev.alpas.auth.console.stubs

import java.util.*

internal class Stubs {
    companion object {
        fun stubFor(name: String?): String {
            return when (name?.lowercase(Locale.getDefault())) {
                "view.header" -> headerViewStub()
                "view.login" -> loginViewStub()
                "view.register" -> registerViewStub()
                "view.verify" -> verifyViewStub()
                "view.email_reset" -> emailResetViewStub()
                "view.email_verify" -> emailVerifyViewStub()
                "view.passwords_reset_email" -> passwordResetEmailViewStub()
                "view.passwords_reset" -> passwordResetViewStub()
                "controller.home" -> homeControllerStub()
                "controller.email_verification" -> emailverificationcontrollerStub()
                "controller.login" -> loginControllerStub()
                "controller.forgot_password" -> forgotPasswordControllerStub()
                "controller.password_reset" -> passwordResetControllerStub()
                "controller.register" -> registerControllerStub()
                "view.home" -> homeViewStub()
                "view.app_layout" -> appLayoutViewStub()
                else -> ""
            }.trimIndent()
        }

        private fun headerViewStub(): String {
            return """
                <a href="/" class="text-5xl font-thin text-center my-10 flex justify-center items-center">
                    <div class="ml-2">{{ env('APP_NAME', 'Alpas') }}</div>
                </a>

                {% if hasFlash('success') %}
                    <div class="bg-green-100 text-green-900 mb-2 p-4 text-lg">{{ flash('success') }}</div>
                {% endif %}
            """.trimIndent()
        }

        private fun loginViewStub(): String {
            return """
                {% extends "../layout/app.peb" %}
                {% block content %}

                    <div class="h-screen py-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.peb" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('login') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hi!</h1>
                                    <h2 class="text-xl text-center font-normal">It's good to see you again :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Log in by entering your information below
                                    </h4>
                                </div>
                                <div>
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required autofocus>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="flex items-center justify-between mt-10">
                                    <button class="bg-indigo-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Log In
                                    </button>

                                    {% if hasRoute('password.request') %}
                                        <div class="inline-block align-baseline text-sm">
                                            Forgot Password?
                                            <a href="{{ route('password.request') }}"
                                               class="text-indigo-600 hover:text-indigo-800 font-bold"> Reset </a>
                                        </div>
                                    {% endif %}
                                </div>
                                {% if hasRoute('register') %}
                                    <div class="text-sm mt-6">
                                        Need an account?
                                        <a class="text-indigo-600 hover:text-indigo-800 font-bold"
                                           href="{{ route('register') }}">Register</a>
                                    </div>
                                {% endif %}
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun registerViewStub(): String {
            return """
                {% extends "../layout/app.peb" %}
                {% block content %}

                    <div class="h-screen py-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.peb" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('register') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hello!</h1>
                                    <h2 class="text-xl text-center font-normal">It's nice to meet you :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Sign up by entering your information below
                                    </h4>
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="name"> Name </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('name', 'border-red-500') }}"
                                           id="name" type="text" name="name" value="{{ old('name') }}" required>
                                    {% if hasError('name') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('name') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="confirm-password"> Confirm Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('confirm_password', 'border-red-500') }}"
                                           id="confirm-password" type="password" name="confirm_password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('confirm_password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Register
                                    </button>

                                </div>
                                <div class="text-sm mt-6">
                                    Already have an account?
                                    <a class="text-indigo-600 hover:text-indigo-800 font-bold" href="{{ route('login') }}">Log in</a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}

            """.trimIndent()
        }

        private fun verifyViewStub(): String {
            return """
                {% extends "../layout/app.peb" %}
                {% block content %}

                    <div class="h-screen py-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.peb" %}
                            <div class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4">
                                <div class="pb-8">
                                    <div class="text-indigo-600 my-12">
                                        <svg aria-hidden="true" focusable="false" class="mx-auto" role="img" height="46"
                                             xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
                                            <path fill="currentColor"
                                                  d="M464 4.3L16 262.7C-7 276-4.7 309.9 19.8 320L160 378v102c0 30.2 37.8 43.3 56.7 20.3l60.7-73.8 126.4 52.2c19.1 7.9 40.7-4.2 43.8-24.7l64-417.1C515.7 10.2 487-9 464 4.3zM192 480v-88.8l54.5 22.5L192 480zm224-30.9l-206.2-85.2 199.5-235.8c4.8-5.6-2.9-13.2-8.5-8.4L145.5 337.3 32 290.5 480 32l-64 417.1z"></path>
                                        </svg>
                                    </div>
                                    <h1 class="text-xl text-center font-normal text-gray-800">Please Verify Your Email</h1>
                                    <p class="text-sm text-center font-normal text-gray-700 text-lg my-8">
                                        We've sent an email to <span class="font-bold">{{ auth.user.email }} </span> with a link to
                                        verify your account.
                                    </p>

                                    <p class="text-sm text-center font-normal text-gray-700 text-lg my-8">
                                        Please follow the link in the email to complete the registration.
                                    </p>

                                    <form action="{{ route('verification.resend') }}" method="post" class="text-center">
                                        {% csrf %}
                                        <button class="hover:text-indigo-700 mt-12 text-indigo-600 text-lg" type="submit">
                                            Resend verification email
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun emailResetViewStub(): String {
            return """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">

                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <meta content="telephone=no" name="format-detection"/>
                    <style type="text/css" data-premailer="ignore">
                        @import url(https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,600,700);
                    </style>

                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #edf2f7;
                            font-size: 15px;
                            line-height: 160%;
                            mso-line-height-rule: exactly;
                            color: #444444;
                            width: 100%;
                        }

                        body {
                            font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;
                        }

                        a:hover {
                            text-decoration: underline;
                        }

                        .btn:hover {
                            text-decoration: none;
                        }

                        a.bg-green:hover {
                            background-color: #56ab00 !important;
                        }

                    </style>
                </head>

                <body style="font-size: 15px; margin: 0; padding: 0; line-height: 23px; mso-line-height-rule: exactly; color: #444444; width: 100%; font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                      bgcolor="#f5f7fb">
                <table width="100%" cellspacing="0" cellpadding="0" bgcolor="#f5f7fb"
                       style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">
                    <tr>
                        <td align="center" valign="top"
                            style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                            <table class="wrap" cellspacing="0" cellpadding="0"
                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; max-width: 640px; text-align: left;">
                                <tr>
                                    <td class="p-sm"
                                        style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 8px;">
                                        <table cellpadding="0" cellspacing="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-lg"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-top: 24px; padding-bottom: 24px;">
                                                    <table cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                        <tr>
                                                            <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                                <h3 style="font-size: 28px; font-weight: 300; color: black;"> {{ env("APP_NAME") }} </h3>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                        <div>
                                            <table class="box" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; border-radius: 3px; -webkit-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); border: 1px solid #f0f0f0;">
                                                <tr>
                                                    <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                        <table cellpadding="0" cellspacing="0"
                                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                            <tr>
                                                                <td class="content pb-0" align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px 0;">
                                                                    <h1 class="text-center m-0 mt-md"
                                                                        style="font-size: 28px; color: #555; font-weight: 500; line-height: 130%; margin: 26px 0 0;"
                                                                        align="center">
                                                                        Reset Password</h1>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 20px 48px;">
                                                                    <p> You recently requested to reset the password for
                                                                        your {{ env("APP_NAME") }} account. Please use the button below
                                                                        to reset your password. Please not that the reset link will
                                                                        expire in about {{ tokenExpirationDuration }}
                                                                        hours.</p>
                                                                </td>
                                                            </tr>

                                                            <tr>
                                                                <td align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 0px">
                                                                    <table cellpadding="0" cellspacing="0" border="0" bgcolor="#5EBA00"
                                                                           class="bg-green rounded w-auto"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: separate; width: auto; color: #ffffff; border-radius: 3px;">
                                                                        <tbody>
                                                                        <tr>
                                                                            <td align="center" valign="top" class="lh-1"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; line-height: 100%;">
                                                                                <a href="{{ resetUrl }}"
                                                                                   class="btn bg-green border-blue"
                                                                                   style="color: #ffffff; padding: 12px 32px; border: 1px solid #5EBA00; text-decoration: none; white-space: nowrap; font-weight: 600; font-size: 16px; border-radius: 3px; line-height: 100%; display: block; -webkit-transition: .3s background-color; transition: .3s background-color; background-color: #5EBA00;">
                                                                                        <span class="btn-span"
                                                                                              style="color: #ffffff; font-size: 16px; text-decoration: none; white-space: nowrap; font-weight: 600; line-height: 100%;">Reset Password</span>
                                                                                </a>
                                                                            </td>
                                                                        </tr>
                                                                        </tbody>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 10PX 48px;">
                                                                    <p>If you received this email by mistake, you can simply ignore
                                                                        it. </p>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content pt-0"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 0px 48px;">
                                                                    <table cellspacing="0" cellpadding="0"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                                        <tr>
                                                                            <td class="va-middle"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                                                                                valign="middle"> Thank you! <br/>
                                                                                <span class="text-muted"
                                                                                      style="color: #666;">Team {{ env("APP_NAME") }}</span>
                                                                            </td>
                                                                        </tr>
                                                                    </table>
                                                                </td>

                                                            </tr>

                                                            <tr>
                                                                <td class="content text-muted pt-0 text-center font-sm"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; color: #9eb0b7; font-size: 13px; padding: 20px 48px; margin-top: 20px"
                                                                    align="center"> If you are having trouble with the button above?
                                                                    Please <a href="{{ resetUrl }}">copy
                                                                        this URL</a> and paste it into your browser.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <table cellspacing="0" cellpadding="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-xl"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px">
                                                    <table class="font-sm text-center text-muted" cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; color: #9eb0b7; text-align: center; font-size: 13px;">
                                                        <tr>
                                                            <td align="center" class="pb-md"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-bottom: 16px;">

                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="px-lg"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-right: 24px; padding-left: 24px;">
                                                                Copyright © 2020 {{ env("APP_NAME") }}, All rights reserved.
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
            """.trimIndent()
        }

        private fun emailVerifyViewStub(): String {
            return """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">

                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <meta content="telephone=no" name="format-detection"/>
                    <style type="text/css" data-premailer="ignore">
                        @import url(https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,600,700);
                    </style>

                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #edf2f7;
                            font-size: 15px;
                            line-height: 160%;
                            mso-line-height-rule: exactly;
                            color: #444444;
                            width: 100%;
                        }

                        body {
                            font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;
                        }

                        a:hover {
                            text-decoration: underline;
                        }

                        .btn:hover {
                            text-decoration: none;
                        }

                        a.bg-green:hover {
                            background-color: #56ab00 !important;
                        }

                    </style>
                </head>

                <body style="font-size: 15px; margin: 0; padding: 0; line-height: 23px; mso-line-height-rule: exactly; color: #444444; width: 100%; font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                      bgcolor="#f5f7fb">
                <table width="100%" cellspacing="0" cellpadding="0" bgcolor="#f5f7fb"
                       style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">
                    <tr>
                        <td align="center" valign="top"
                            style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                            <table class="wrap" cellspacing="0" cellpadding="0"
                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; max-width: 640px; text-align: left;">
                                <tr>
                                    <td class="p-sm"
                                        style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 8px;">
                                        <table cellpadding="0" cellspacing="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-lg"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-top: 24px; padding-bottom: 24px;">
                                                    <table cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                        <tr>
                                                            <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                                <h3 style="font-size: 28px; font-weight: 300; color: black;"> {{ env("APP_NAME") }} </h3>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                        <div>
                                            <table class="box" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; border-radius: 3px; -webkit-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); border: 1px solid #f0f0f0;">
                                                <tr>
                                                    <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                        <table cellpadding="0" cellspacing="0"
                                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                            <tr>
                                                                <td class="content pb-0" align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px 0;">
                                                                    <h1 class="text-center m-0 mt-md"
                                                                        style="font-size: 28px; color: #555; font-weight: 500; line-height: 130%; margin: 26px 0 0;"
                                                                        align="center">
                                                                        Thank you for registering, {{ user.name }}! </h1>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 20px 48px;">
                                                                    <p> There's only one step left before you can start
                                                                        using {{ env("APP_NAME") }}. Please verify your email address by
                                                                        clicking the button below.</p>
                                                                </td>
                                                            </tr>

                                                            <tr>
                                                                <td align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 0px">
                                                                    <table cellpadding="0" cellspacing="0" border="0"
                                                                           class="bg-green rounded w-auto"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: separate; width: auto; color: #ffffff; border-radius: 3px;"
                                                                           bgcolor="#5EBA00">
                                                                        <tbody>
                                                                        <tr>
                                                                            <td align="center" valign="top" class="lh-1"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; line-height: 100%;">
                                                                                <a href="{{ verificationUrl }}"
                                                                                   class="btn bg-green border-blue"
                                                                                   style="color: #ffffff; padding: 12px 32px; border: 1px solid #5EBA00; text-decoration: none; white-space: nowrap; font-weight: 600; font-size: 16px; border-radius: 3px; line-height: 100%; display: block; -webkit-transition: .3s background-color; transition: .3s background-color; background-color: #5EBA00;">
                                                                                        <span class="btn-span"
                                                                                              style="color: #ffffff; font-size: 16px; text-decoration: none; white-space: nowrap; font-weight: 600; line-height: 100%;">Confirm email address</span>
                                                                                </a>
                                                                            </td>
                                                                        </tr>
                                                                        </tbody>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 10PX 48px;">
                                                                    <p>If you received this email by mistake, you can simply ignore
                                                                        it. </p>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content pt-0"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 0px 48px;">
                                                                    <table cellspacing="0" cellpadding="0"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                                        <tr>
                                                                            <td class="va-middle"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                                                                                valign="middle"> Thank you! <br/>
                                                                                <span class="text-muted"
                                                                                      style="color: #666;">Team {{ env("APP_NAME") }}</span>
                                                                            </td>
                                                                        </tr>
                                                                    </table>
                                                                </td>

                                                            </tr>

                                                            <tr>
                                                                <td class="content text-muted pt-0 text-center font-sm"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; color: #9eb0b7; font-size: 13px; padding: 20px 48px; margin-top: 20px"
                                                                    align="center"> If you are having trouble with the button above?
                                                                    Please <a href="{{ verificationUrl }}">copy this URL</a> and paste
                                                                    it into your browser.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <table cellspacing="0" cellpadding="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-xl"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px">
                                                    <table class="font-sm text-center text-muted" cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; color: #9eb0b7; text-align: center; font-size: 13px;">
                                                        <tr>
                                                            <td align="center" class="pb-md"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-bottom: 16px;">

                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="px-lg"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-right: 24px; padding-left: 24px;">
                                                                Copyright © 2020 {{ env("APP_NAME") }}, All rights reserved.
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
            """.trimIndent()
        }

        private fun passwordResetEmailViewStub(): String {
            return """
                {% extends "../../layout/app.peb" %}
                {% block content %}

                    <div class="h-screen py-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "../_header.peb" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('password.email') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hi!</h1>
                                    <h2 class="text-xl text-center font-normal">We are here to help :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Enter your email address to reset your password
                                    </h4>
                                </div>
                                <div>
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required autofocus>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Send Password Reset Link
                                    </button>
                                </div>
                                <div class="text-sm mt-6">
                                    <a class="text-indigo-600 hover:text-indigo-800 font-bold" href="{{ route('login') }}">
                                        &leftarrow; Back to Log in
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun passwordResetViewStub(): String {
            return """
                {% extends "../../layout/app.peb" %}
                {% block content %}

                    <div class="h-screen py-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "../_header.peb" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('password.update') }}">
                                {% csrf %}
                                <input type="hidden" name="token" value="{{ token }}">
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Welcome back!</h1>
                                    <h2 class="text-xl text-center font-normal">Happy to see you :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        To continue, please set a strong password.
                                    </h4>
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="confirm-password"> Confirm Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('confirm_password', 'border-red-500') }}"
                                           id="confirm-password" type="password" name="confirm_password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('confirm_password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Continue
                                    </button>

                                </div>
                                <div class="text-sm mt-6">
                                    <a class="text-indigo-500 hover:text-indigo-800 font-bold" href="{{ route('login') }}">
                                        &leftarrow; Back to Log in
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun homeControllerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.middleware.VerifiedEmailOnlyMiddleware
                import dev.alpas.http.HttpCall
                import dev.alpas.routing.Controller
                import dev.alpas.routing.ControllerMiddleware

                class StubClazzName : Controller() {
                    override fun middleware(call: HttpCall) = listOf(ControllerMiddleware(VerifiedEmailOnlyMiddleware::class))

                    fun index(call: HttpCall) {
                        call.render("home")
                    }
                }
            """.trimIndent()
        }

        private fun emailverificationcontrollerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.HandlesEmailVerification
                import dev.alpas.routing.Controller

                class StubClazzName : Controller(), HandlesEmailVerification
            """.trimIndent()
        }

        private fun forgotPasswordControllerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.HandlesForgottenPassword
                import dev.alpas.routing.Controller

                class StubClazzName : Controller(), HandlesForgottenPassword
            """.trimIndent()
        }

        private fun loginControllerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.HandlesUserLogin
                import dev.alpas.routing.Controller

                class StubClazzName : Controller(), HandlesUserLogin
            """.trimIndent()
        }

        private fun passwordResetControllerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.HandlesPasswordReset
                import dev.alpas.routing.Controller

                class StubClazzName : Controller(), HandlesPasswordReset
            """.trimIndent()
        }

        private fun registerControllerStub(): String {
            return """
                package StubPackageName

                import dev.alpas.auth.HandlesUserRegistration
                import dev.alpas.routing.Controller

                class StubClazzName : Controller(), HandlesUserRegistration
            """.trimIndent()
        }

        private fun homeViewStub(): String {
            return """
                {% extends "layout/app.peb" %}
                {% block content %}
                    <div class="h-screen">
                        <div class="bg-white max-w-4xl mt-10 mx-auto p-10 pt-10 w-full">
                            <h3 class="font-medium mb-3 text-2xl">Welcome, {{ auth.user.name }}! </h3>
                            This is your
                            <pre class="bg-gray-200 inline-flex px-1">resources/templates/home</pre>
                            page.
                            <p class="text-gray-800 mt-4">Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusamus assumenda
                                dicta incidunt molestiae, nesciunt officiis quas quos. Autem consequuntur corporis, dolore eos inventore magnam mollitia numquam
                                pariatur, porro, similique temporibus! Lorem ipsum dolor sit amet, consectetur adipisicing elit. Adipisci
                                aliquam asperiores aut, dolorem eum, illo in natus nesciunt nihil optio porro praesentium provident quaerat,
                                quidem sint totam veniam vero vitae. Lorem ipsum dolor sit amet, consectetur adipisicing elit. A atque
                                deleniti facilis fugiat harum impedit magni minus molestiae perspiciatis, provident quisquam, quos rem sint
                                suscipit tenetur totam vel veniam vitae.</p>

                            <div class="flex font-medium justify-around links mt-10 mt-lg mx-16 uppercase">
                                <a href="https://alpas.dev/"><span class="text-gray-600">/</span> Alpas</a>
                                <a href="https://alpas.dev/docs"><span class="text-gray-600">/</span> Documentation</a>
                                <a href="https://twitter.com/alpasdev"><span class="text-gray-600">/</span>@AlpasDev</a>
                                <a href="https://www.youtube.com/channel/UCGCGb-vvmff3csy8dePBJJQ"><span class="text-gray-600">/</span> AlpasCasts</a>
                                <a href="https://github.com/alpas"><span class="text-gray-600">/</span> GitHub</a>
                            </div>
                        </div>
                    </div>
                {% endblock %}
            """.trimIndent()
        }

        private fun appLayoutViewStub(): String {
            return """
                <!doctype html>
                <html lang="en">

                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                    <link rel="stylesheet" href="{{ mix('css/app.css') }}">
                    <title>🚀 Alpas</title>
                    <style>
                        #menu-toggle:checked + #menu {
                            display: block;
                        }
                    </style>
                </head>

                <body class="antialiased bg-gray-200 h-full">
                <div id="app">
                    {% if not routeIsOneOf(['login', 'register', 'password.request', 'password.reset']) %}
                        <header class="lg:px-16 px-6 bg-white flex flex-wrap items-center py-3">
                            <div class="flex-1 flex justify-between items-center">
                                <a href="/" class="text-xl font-medium"> 🚀 Alpas</a>
                            </div>

                            <label for="menu-toggle" class="pointer-cursor lg:hidden block">
                                <svg class="fill-current text-gray-900" xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                                     viewBox="0 0 20 20"><title>menu</title>
                                    <path d="M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z"></path>
                                </svg>
                            </label>
                            <input class="hidden" type="checkbox" id="menu-toggle"/>

                            <div class="hidden lg:flex lg:items-center lg:w-auto w-full" id="menu">
                                <div class="flex flex-col-reverse lg:flex lg:flex-row lg:w-auto w-full mt-6 lg:mt-0">
                                    {% guest %}
                                        <nav>
                                            <ul class="lg:flex items-center justify-between text-base text-gray-700 pt-4 lg:pt-0">
                                                <li>
                                                    <a class="lg:p-4 py-3 px-0 block border-b-2 border-transparent hover:border-indigo-400"
                                                       href="{{ route('login') }}">
                                                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                                             stroke-linejoin="round" class="inline">
                                                            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                                                            <polyline points="10 17 15 12 10 7"></polyline>
                                                            <line x1="15" y1="12" x2="3" y2="12"></line>
                                                        </svg>
                                                        Log in
                                                    </a>
                                                </li>
                                                <li>
                                                    <a class="lg:p-4 py-3 px-0 block border-b-2 border-transparent hover:border-indigo-400"
                                                       href="{{ route('register') }}">
                                                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                                             stroke-linejoin="round" class="inline">
                                                            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                                            <circle cx="8.5" cy="7" r="4"></circle>
                                                            <line x1="20" y1="8" x2="20" y2="14"></line>
                                                            <line x1="23" y1="11" x2="17" y2="11"></line>
                                                        </svg>
                                                        Register
                                                    </a>
                                                </li>
                                            </ul>
                                        </nav>
                                        {% else %}
                                        <form action="{{ route('logout') }}" method="post">
                                            {% csrf %}
                                            <button class="bg-transparent hover:bg-indigo-600 text-blue-700 font-semibold hover:text-white py-2 px-4 border border-blue-500 hover:border-transparent rounded mr-4"
                                                    type="submit">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                     fill="none"
                                                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                                                     class="inline mr-1">
                                                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                                    <polyline points="16 17 21 12 16 7"></polyline>
                                                    <line x1="21" y1="12" x2="9" y2="12"></line>
                                                </svg>
                                                Log Out
                                            </button>
                                        </form>
                                    {% endguest %}
                                    {% auth %}
                                        <div class="lg:ml-4 flex items-center justify-start lg:mb-0 mb-4 pointer-cursor border-b lg:border-0 pb-4 lg:pb-0">
                                            <img class="rounded-full w-10 h-10 border-2 border-gray-300"
                                                 src="{{ auth.user.gravatarUrl() }}" alt="{{ auth.user.email }}">
                                            <span class="ml-3 text-gray-700">{{ auth.user.email }}</span>
                                        </div>
                                    {% endauth %}
                                </div>
                            </div>
                        </header>
                    {% endif %}
                    <main>
                        {% block content %} {% endblock %}
                    </main>
                </div>
                <script src="{{ mix('js/app.js') }}"></script>
                </body>
                </html>
            """.trimIndent()
        }
    }
}
