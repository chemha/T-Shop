$(window).on("load", () => {
  $("#create").submit(function(event) {
    event.preventDefault();

    const formData = {
      "category_id": parseInt($("#category_id").val()),
      "title": $("#title").val(),
      "price": parseFloat($("#price").val()),
      "brand": $("#brand").val(),
      "color": $("#color").val(),
      "weight": parseFloat($("#weight").val()),
      "volume": parseFloat($("#volume").val()),
      "quantity": parseInt($("#quantity").val())
    };

    console.log(formData);

    $.ajax({
      url: "/products",
      type: "POST",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Creation successful!");
        window.location.href = "/";
      },
      error: function(e) {
        alert("Creation failed!");
        console.log("ERROR: ", e);
      }
    });
  });
});