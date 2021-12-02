function() {
    const baseUrl = 'https://api.dev.nva.aws.unit.no/path-from-config/'
    console.log(' url: ' + baseUrl)

    // Make GitHub Action input variables available as Karata script variables
    var config = {
        url: baseUrl,
    };

    // These can be set in test (https://github.com/intuit/karate#configure)
    // karate.configure("ssl", true)
    // karate.configure('connectTimeout', 10000);
    // karate.configure('readTimeout', 10000);

    return config;
}
