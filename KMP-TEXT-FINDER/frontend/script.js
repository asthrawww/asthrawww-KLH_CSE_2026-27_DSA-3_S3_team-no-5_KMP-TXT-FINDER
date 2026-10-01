async function searchPattern() {

    const text =
        document.getElementById("text").value;

    const pattern =
        document.getElementById("pattern").value;


    if (text === "" || pattern === "") {

        alert(
            "Please enter both text and pattern."
        );

        return;
    }


    const data =
        new URLSearchParams();

    data.append("text", text);

    data.append("pattern", pattern);


    try {

        const response =
            await fetch(
                "http://localhost:8080/search",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body: data
                }
            );


        const result =
            await response.json();


        if (result.error) {

            alert(result.error);

            return;
        }


        document.getElementById(
            "results"
        ).style.display = "block";


        document.getElementById(
            "occurrences"
        ).textContent =
            result.occurrences;


        document.getElementById(
            "time"
        ).textContent =
            result.time.toFixed(6)
            + " ms";


        document.getElementById(
            "textLength"
        ).textContent =
            result.textLength;


        document.getElementById(
            "patternLength"
        ).textContent =
            result.patternLength;


        document.getElementById(
            "positions"
        ).textContent =
            result.positions.length > 0
                ? result.positions.join(", ")
                : "No match found";


        document.getElementById(
            "lps"
        ).textContent =
            result.lps.join(", ");


    } catch (error) {

        alert(
            "Cannot connect to Java backend.\n\n" +
            "Make sure the Java server is running."
        );

        console.error(error);
    }
}


function clearAll() {

    document.getElementById(
        "text"
    ).value = "";


    document.getElementById(
        "pattern"
    ).value = "";


    document.getElementById(
        "results"
    ).style.display = "none";
}
