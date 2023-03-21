$(window).on("load", () => {
  $("#register").submit(function(event) {
    event.preventDefault();

    const formData = {
      "name": $("#name").val(),
      "surname": $("#surname").val(),
      "birthdate": $("#birthdate").val(),
      "email": $("#email").val(),
      "password": $("#password").val()
    };

    $.ajax({
      url: "/clients",
      type: "POST",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Registration successful!");
        window.location.href = "/clients/login";
      },
      error: function(e) {
        alert("Registration failed!");
        console.log("ERROR: ", e);
      }
    });
  });
});