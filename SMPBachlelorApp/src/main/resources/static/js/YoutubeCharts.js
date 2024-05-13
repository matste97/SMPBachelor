function createChart(insightsData) {
    // Define a mapping object for label translations and descriptions
    const labelTranslations = {
        "impressions": { label: "Inntrykk", description: "Totalt antall ganger media har blitt sett på kontoen." },
        "reach": { label: "Unike inntrykk", description: "Totalt antall unike intrykk på kontoen." },
        "profile_views": { label: "Profilvisninger", description: "Totalt antall brukere som har sett på profilen siste døgnet." }
    };

    // Map the original labels to their translated versions
    var labels = insightsData.map(function(item) {
        return labelTranslations[item.name].label;
    });

    // Define an array to hold the tooltips for each label
    var tooltips = insightsData.map(function(item) {
        return labelTranslations[item.name].description;
    });

    var values = insightsData.map(function(item) {
        return item.values.map(function(value) {
            return value.value;
        });
    }).flat();

    var ctx = document.getElementById('insightsChart').getContext('2d');
    var myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Insights Data',
                data: values,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                ],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            },
            tooltips: {
                callbacks: {
                    // Customize tooltip text
                    label: function(tooltipItem, data) {
                        var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                        return labelTranslations[data.labels[tooltipItem.index]].label + ': ' + value;
                    },
                    // Add label description to tooltip title
                    title: function(tooltipItem, data) {
                        return tooltips[tooltipItem[0].index];
                    }
                }
            }
        }
    });
}
