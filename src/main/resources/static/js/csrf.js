// Sends the CSRF token that Spring Security stores in the XSRF-TOKEN cookie with every AJAX request
$.ajaxSetup({
    beforeSend: function (xhr) {
        const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
        if (match) xhr.setRequestHeader("X-XSRF-TOKEN", decodeURIComponent(match[1]));
    }
});
