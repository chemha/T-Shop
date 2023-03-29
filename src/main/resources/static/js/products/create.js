$(window).on("load", () => {
  $("#create").submit(function(event) {
    event.preventDefault();

    let weight = parseFloat($("#weight").val());
    let volume = parseFloat($("#volume").val());

    if (weight == null || isNaN(weight)) weight = 0;
    if (volume == null || isNaN(volume)) volume = 0;

    const formData = {
      "category_id": parseInt($("#category_id").val()),
      "title": $("#title").val(),
      "price": parseFloat($("#price").val()),
      "brand": $("#brand").val(),
      "color": $("#color").val(),
      "weight": weight,
      "volume": volume,
      "quantity": parseInt($("#quantity").val())
    };

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