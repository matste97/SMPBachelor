from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
from googleapiclient.discovery import build
import json
import os

# Set up OAuth 2.0 credentials
SCOPES = ['https://www.googleapis.com/auth/youtube.readonly','https://www.googleapis.com/auth/yt-analytics.readonly']
CLIENT_SECRETS_FILE = ''  # Path to the client secret file, replace with path to yours

def get_authenticated_service(api_name, api_version, scopes):
    creds = None
    if os.path.exists('token.json'):
        creds = Credentials.from_authorized_user_file('token.json', scopes)
    # If there are no (valid) credentials available, let the user log in.
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                CLIENT_SECRETS_FILE, scopes)
            creds = flow.run_local_server(port=8080)
        # Save the credentials for the next run
        with open('token.json', 'w') as token:
            token.write(creds.to_json())
    return build(api_name, api_version, credentials=creds)

def get_authenticated_analytics_service():
    return get_authenticated_service('youtubeAnalytics', 'v2', SCOPES)

def get_authenticated_data_service():
    return get_authenticated_service('youtube', 'v3', SCOPES)

# Get age and gender analytics for a specific video
def get_age_gender_analytics(youtube_analytics, video_id):
    analytics_response = youtube_analytics.reports().query(
        ids="channel==MINE",
        startDate="2013-01-01",
        endDate="2024-03-14",
        metrics="viewerPercentage",
        dimensions="ageGroup,gender",
        filters=f"video=={video_id}"
    ).execute()
    return analytics_response



# Get title of a specific video
def get_video_title(youtube_data, video_id):
    video_response = youtube_data.videos().list(
        part='snippet',
        id=video_id
    ).execute()
    return video_response['items'][0]['snippet']['title']

def get_video_ids(youtube_data):
    playlist_request = youtube_data.channels().list(
        part='contentDetails',
        mine=True,
    )

    playlist_response = playlist_request.execute()
    playlist_id = playlist_response['items'][0]['contentDetails']['relatedPlaylists']['uploads']
    videos_request = youtube_data.playlistItems().list(
        part='contentDetails',
        playlistId=playlist_id,
        maxResults=50 # You may need to paginate if there are more than 50 videos
    )

    videos_response = videos_request.execute()

    # Create a list to store video IDs
    video_ids = [item['contentDetails']['videoId'] for item in videos_response['items']]
    return (video_ids)

def get_channel_analytics(youtube_analytics):
    request = youtube_analytics.reports().query(
        ids='channel==mine',
        startDate='2015-01-01',
        endDate='2024-01-26',
        metrics='viewerPercentage',
        dimensions='ageGroup,gender'
    )
    response = request.execute()
    # Now you can access the data in the 'response' variable
    return response

# Main function
def main():
    youtube_analytics = get_authenticated_analytics_service()
    youtube_data = get_authenticated_data_service()

    channel_analytics_data = get_channel_analytics(youtube_analytics)
    print("Here comes channel analytics: ")
    for header in channel_analytics_data['columnHeaders']:
        print(header['name'].capitalize(), end='\t')
    print()

# Print analytics data
    for row in channel_analytics_data['rows']:
        for i, value in enumerate(row):
            print(value, end='\t\t') if i != len(row) - 1 else print(value)
    print("----------------")

    # Commented out code is for looping through all videos, since I don't have much data I'll just use get data for one video
    # video_ids = get_video_ids(youtube_data)
    
    # loop through all
    # for video_id in video_ids:

    # # Get video title
    #     title = get_video_title(youtube_data, video_id)
    #     print("Video Title:", title)
    
    #     # Get age and gender analytics
    #     analytics = get_age_gender_analytics(youtube_analytics, video_id)
    #     print("Analytics Data:")
    #     print(json.dumps(analytics, indent=4))



    #Print all video IDs
    print("Here comes list of all video IDs: ")
    print(get_video_ids(youtube_data))
    print("----------------")
    

    # Get data from one video
    video_id = '' # Replace with ID for video you want data from or uncomment earlier code to go through all video IDs without having to specify
    # Get video title
    title = get_video_title(youtube_data, video_id)
    print("Video Title:", title)
    
    # Get age and gender analytics
    analytics = get_age_gender_analytics(youtube_analytics, video_id)
    print("Video analytics Data:")
    print(json.dumps(analytics, indent=4))

if __name__ == '__main__':
    main()
