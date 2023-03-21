$(window).on("load", () => {
  $("#edit").submit(function(event) {
    event.preventDefault();
    const id = $("#id").val();

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
      url: "/products/" + id,
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Edit successful!");
        window.location.href = "/";
      },
      error: function(e) {
        alert("Edit failed!");
        console.log("ERROR: ", e);
      }
    });
  });
});