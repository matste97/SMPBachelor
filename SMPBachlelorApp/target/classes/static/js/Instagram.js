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
        return item.values[0].value; // Adjusted to directly access the 'value' property
    });

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

function createAgeAndGenderChart(data) {
    // Extracting age groups and counts
    const ageGroups = Object.keys(data);
    const counts = Object.values(data);

    // Define custom sorting function
    function customSort(a, b) {
        const ageA = parseInt(a.split('.')[1].split('-')[0]);
        const ageB = parseInt(b.split('.')[1].split('-')[0]);
        const genderOrder = { 'M': 1, 'F': 2, 'U': 3 }; 

        if (ageA !== ageB) {
            return ageA - ageB; 
        } else {
            const genderA = a.split('.')[0];
            const genderB = b.split('.')[0];
            return genderOrder[genderA] - genderOrder[genderB];
        }
    }

    // Sort age groups and genders
    ageGroups.sort(customSort);

    // Define colors for each gender
    const colors = {
        'M': 'rgba(54, 162, 235, 0.2)', // Blue
        'F': 'rgba(255, 99, 132, 0.2)', // Red
        'U': 'rgba(255, 206, 86, 0.2)' // Yellow
    };

    // Creating data for Chart.js
    const chartData = {
        labels: ageGroups,
        datasets: [{
            label: 'Alder og kjønn til følgere av Sunnmoersposten',
            data: counts,
            backgroundColor: ageGroups.map(label => colors[label.split('.')[0]]),
            borderColor: ageGroups.map(label => colors[label.split('.')[0]].replace('0.2', '1')),
            borderWidth: 1
        }]
    };

    // Configuring options for the chart
    const chartOptions = {
        scales: {
            y: {
                beginAtZero: true
            }
        }
    };

    // Creating the chart
    const ctx = document.getElementById('ageAndGenderChart').getContext('2d');
    const ageAndGenderChart = new Chart(ctx, {
        type: 'bar',
        data: chartData,
        options: chartOptions
    });
}

// Call the function with the data
createAgeAndGenderChart(ageAndGenderData);
