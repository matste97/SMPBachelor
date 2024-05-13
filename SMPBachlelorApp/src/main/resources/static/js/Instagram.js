function createChart(insightsData) {
    const labelTranslations = {
        "impressions": "Inntrykk",
        "reach": "Unike inntrykk",
        "profile_views": "Profilvisninger"
    };

    // Map the original labels to their translated versions
    var labels = insightsData.map(function(item) {
        return labelTranslations[item.name];
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
                label: 'Sunnmoersposten',
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
            }
        }
    });
}