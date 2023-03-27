function productDeletion(id) {
    $.ajax({
            url: "/products/" + id,
            type: "DELETE",
            success: function(response) {
            alert("Deletion successful!");
            window.location.href = "/";
        },
            error: function(e) {
            alert("Deletion failed!");
            console.log("ERROR: ", e);
        }
    });
}

function confirmDeletion(id) {
    swal({
        title: "¿Are you sure you want to delete this record?",
        text: "This can't be undone",
        icon: "warning",
        buttons: ["Cancel", "Delete"],
        dangerMode: true,
    }).then((confirmed) => {
        if (confirmed) productDeletion(id);
    });
}

$(window).on("load", () => {
    const table = $("#product-table").DataTable({
        responsive: true
    });
});