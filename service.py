# -*- coding: utf-8 -*-

'''
    Skip Credits Add-on

    Copyright roguetech.

    It is distributed WITHOUT ANY WARRANTY or any implied warranty of
    MERCHANTABILITY.
'''

import xbmcvfs,xbmc,xbmcaddon,os,xbmcgui, time, re
KODI_VERSION = int(xbmc.getInfoLabel("System.BuildVersion").split(".")[0])
ADDON = xbmcaddon.Addon()
KODIMONITOR = xbmc.Monitor()

def setDebug():
    return True

def log(msg):
    if setDebug:
        xbmc.log(u'{0}: {1}'.format('scripts.skipcredits', msg), level=xbmc.LOGFATAL)

class Player(xbmc.Player):
    def __init__(self, *args):
        addonName = 'Skip Credits'

    def extract_season(self,text):
        text = text.lower()
        # Use a regular expression to extract the season and episode numbers
        match = re.search(r'(s|season)(\d+)', text, re.I)
        if match:
            return int(match.group(2))
        else:
            return None

    def extract_episode(self,text):
        text = text.lower()
        # Use a regular expression to extract the season and episode numbers
        match = re.search(r'(e|episode|ep)(\d+)', text, re.I)
        if match:
            return int(match.group(2))
        else:
            return None

    #text1 is for video name
    #text2 may either be timestamp file name, or contents of it
    def match_season(self,text1,text2):
        text1_season = self.extract_season(text1)

        #If no video season, return true
        if not text1_season: return True

        pattern = r'(s|season)(\d+)'
        matches = re.findall(pattern, text2, re.I)
        if not matches: return text2

        line_seasons = [int(match[1]) for match in matches]
        if text1_season in line_seasons: return text2

    #text1 is for video name
    #text2 may either be timestamp file name, or contents of it
    def match_episode(self,text1,text2):
        text1_episode = self.extract_episode(text1)

        #If no video season, return true
        if not text1_episode: return True

        pattern = r'(e|episode)(\d+)'
        matches = re.findall(pattern, text2, re.I)
        if not matches: return text2

        line_episodes = [int(match[1]) for match in matches]
        if text1_episode in line_episodes: return text2

    def check_season_episode_match(self, videoname, text):
        if self.match_season(videoname,text) and self.match_episode(videoname,text):
            return True

    # Performs xbmc.getPlayingFile()
    # Returns null if video not playing
    def getVideoFolder(self):
        from contextlib import closing
        folderName = None
        try:
            folderName = xbmc.getInfoLabel('Player.FilenameAndPath')
            log(folderName)
        except RuntimeError:
            pass
        return folderName
    
    
    # Performs xbmc.getPlayingFile()
    # Returns null if video not playing
    def getVideoName(self):
        from contextlib import closing
        videoName = None
        try:
            videoName = xbmc.Player().getPlayingFile()
        except RuntimeError:
            pass
        return videoName
    
    # Performs xbmc.getTime()
    # Returns null if video not playing
    def getRuntime(self):
        from contextlib import closing
        runTime = float(0)
        try:
            runTime = float(xbmc.Player().getTime())
        except RuntimeError:
            pass
        return runTime
    
    # Performs xbmc.getTime()
    # Returns null if video not playing
    def getVideoLength(self):
        from contextlib import closing
        videoLength = float(0)
        try:
            videoLength = float(xbmc.Player().getTotalTime())
        except RuntimeError:
            pass
        return videoLength


    # Performs xbmc.seekTime()
    # Returns null if video not playing
    def setSeek(self,time):
        from contextlib import closing
        try:
            if time == 999999:
                time = self.getRuntime
            xbmc.Player().seekTime(float(time))
            return True
        except RuntimeError:
            pass

    def check_playnext(self):
        from contextlib import closing
        try:
            playlist = xbmc.PlayList(xbmc.PLAYLIST_VIDEO)
            if playlist.size() < 2:
                return False
            if playlist.getposition() < (playlist.size() - 1):
                return True
        except RuntimeError:
            pass

        
    def play_next(self):
        from contextlib import closing
        try:
            xbmc.Player().playnext()
        except RuntimeError:
            pass

    def stop_play(self):
        from contextlib import closing
        try:
            xbmc.Player().stop()
        except RuntimeError:
            pass


    # Return true if file name matches `skip.txt`, or contains video name while ending in `skip.txt`, as case-insensative
    def check_file_match(self, filename, videoname):
        # Either can be one file with each season/episode enumerated,
        # or separate files named by season and episode
        if not filename.lower().endswith('skip.txt'):  return
        
        if filename.lower() == 'skip.txt': return True
        if videoname.lower() in filename: return True

    def read_file(self, foldername, filename):
        from contextlib import closing
        try:
            file_path = os.path.join(foldername, filename)
            with xbmcvfs.File(file_path,'r') as f:
                return f.read()
        except IOError:
            log('Unable to open file:' + foldername + '\\' + filename)
            pass

    def read_time_line(self,line):
        # Timestamps
        if not re.match(r'^(\d+:)?(\d+:)?(\d+:)?\d+(-(\d+:)?(\d+:)?(\d+:)?\d+)?',line): return
        # If timestamp ends with `-`, remove it
        if line.endswith('-'): line[:-1]
        stop = 0
        if "-" in line:
            start = line.rsplit("-", 1)[0]
            stop = line.rsplit("-", 1)[1]
        else:
            start = line
        start = self.convertTimeToSeconds(start)
        # Weird way to check if stop is only seconds
        newStop = 0
        if stop:
            newStop = self.convertTimeToSeconds(stop)   # convert to seconds
            if newStop == stop:                         # If unchanged, must be just seconds
                newStop = float(newStop) + float(start)     # In which case, add it to start time
        stop = newStop
        # Format to 7 digits for sort to work
        # (enough for 11 days)
        return str(start).rjust(10, '0') + '-' + str(stop).rjust(10, '0').strip()
    
    def get_timestamps(self):
        foldername = self.getVideoFolder()
        if not foldername: return
        foldername = os.path.dirname(foldername)
        # Need video name for season/episode match in timestamp file
        videoname  = self.getVideoName()
        if not videoname: return
        videoname = os.path.basename(videoname).rsplit(".", 1)[0]

        if "." in videoname:
            videoname = videoname.rsplit(".", 1)[0]
        log('Playing ' + videoname)

        # Get season number and episode numbers for playing file
        video_season = self.extract_season(videoname)
        video_episode = self.extract_episode(videoname)

        if not foldername or not videoname: return

        timestamps = []
        files = os.listdir(foldername)

        isMatch = True
        for filename in files:
            if not self.check_file_match(filename,videoname):
                continue
            log('Using timestamps in ' + filename + '.')

            text = self.read_file(foldername,filename)
            if not text:
                continue
            lines = text.split('\n')
            for line in lines:
                line = line.strip()

                # skip lines that start with '#' or are empty
                # would prefer allowing inline comments too
                if not line or line.startswith('#'): continue

                #season/episode
                if re.search(r'^s\d{2}', line):
                    isMatch = False
                    if self.check_season_episode_match(videoname, line):
                        isMatch = True
                    continue

                # Timestamps
                if not isMatch: continue
                if self.read_time_line(line):
                    timestamps.append(self.read_time_line(line))
        timestamps.sort()
        return timestamps

    def convertTimeToSeconds(self, strTime):
        if not ':' in str(strTime):
            return strTime
        time_array = strTime.split(':')
        time_array .reverse()
        seconds = time_array [0]
        minutes = time_array [1] if len(time_array) > 1 else 0
        hours = time_array [2] if len(time_array) > 2 else 0
        days = time_array [3] if len(time_array) > 3 else 0
        return days * 86400 + int(hours) * 3600 + int(minutes) * 60 + float(seconds)

    def get_start_time(self, time_string):
        if '-' in time_string:
             return float(time_string.rsplit("-", 1)[0])
        return float(time_string)

    def get_stop_time(self, time_string):
        if "-" in time_string:
            return float(time_string.rsplit("-", 1)[1])
        log(time_string + " does contain a dash.")

    def onPlayBackStarted(self):
        # Wait a bit for video to load
        #if KODIMONITOR.waitForAbort(0.4):
        #    return
        filename = self.getVideoName()
        self.timestamps = []
        timestamps = self.get_timestamps()

        if not filename: return
        if not timestamps:
            self.waitVideoEnd(filename)
            return

        # Log all the matching timestamps
        if setDebug():
            log('Got timestamps:')  
            for i in range(0, len(timestamps), 1):
                log(str(i) + ': ' + str(timestamps[i]))

        # Check if video legth is zero
        self.waitVideoPlaytime()
         
        #loop through each timestamp entry (extracted for this one video)
        # After all timestamps, send it to waitVideoEnd
        for i in range(0, len(timestamps), 1):
            # get the (next) timestamp
            start_time = self.get_start_time(timestamps[i])
            stop_time = self.get_stop_time(timestamps[i])
            # If start = 0, and stop is blank, don't skip the entire video!
            if not start_time and not stop_time:
                log('Start and stop is blank.')
                continue

            # If no stop time entered, then set it to skip to next video
            if not stop_time:
                stop_time = 999999
            # If stop time extends past end time, then set it to skip to next video
            # Don't know if this matters
            elif stop_time > self.getVideoLength():
                log('Stop time '+ str(stop_time) + ' is greater than video length ' + str(self.getVideoLength()))
                stop_time = 999999

            log('Start time: ' + str(start_time) + '; end time: ' + str(stop_time))
            log('Waiting for start time: ' + str(start_time))
            if not self.getVideoName: return
            # Wait for skip time to arrive (or video stops)
            while (self.getRuntime() < start_time):
                if KODIMONITOR.waitForAbort(0.2):
                    # Abort was requested while waiting. We should exit
                    return
                # Check if still playing the same video
                if self.getVideoName() != filename: return

            # If already past destination point, skip to the next timestamp
            if (self.getRuntime() and stop_time < self.getRuntime()):
                log('Stop time (' + str(stop_time) + ') has passed (' + str(self.getRuntime()) + ').')
                continue

            log('Skipped from ' + str(self.getRuntime()) + ' (' + str(start_time) + ') to ' + str(stop_time) + ' (' + str(stop_time) + ')')
            # If skipping to the end of the video, play next
            # Then return, since it'll be a new video
            if stop_time == 999999:
                if int(start_time) == 0:
                    log('Trying to skip the entire video with start ' + str(start_time) + ' end ' + str(stop_time))
                    self.waitVideoEnd(filename)
                # Set seek to near end, to set video play as completed (no resume position)
                self.setSeek(self.getVideoLength() - 10)
                # If there is a playlist (and not at the end of it), play next
                if self.check_playnext():
                    log('Skipping to next video.')
                    self.play_next()
                # otherwise, stop
                else:
                    log('Stopping playback.')
                    self.stop_play()
                # Wait for video end (so not to get stuck in a loop, if Kodi doesn't skip/end fast enough)
                self.waitVideoEnd(filename)
                return

            if not self.setSeek(stop_time):
                return
        # After all timestamps, send it to waitVideoEnd
        self.waitVideoEnd(filename)
        
    def waitVideoPlaytime(self):
        # If after last time stamp, monitor for abort or new video
        log('Waiting for video to properly start.')
        if not xbmc.Player().isPlaying():
            return
        while self.getVideoLength() == 0:
                if KODIMONITOR.waitForAbort(1):
                    break

    def waitVideoEnd(self, filename):
        # If after last time stamp, monitor for abort or new video
        log('Waiting for video to end.')
        while self.getVideoName() == filename:
            if KODIMONITOR.waitForAbort(1):
                break

    def ServiceEntryPoint(self):
        while not KODIMONITOR.abortRequested():
            if xbmc.Player().isPlaying():
                self.onPlayBackStarted()
            else:
                # check every 1 sec while not playing
                if KODIMONITOR.waitForAbort(1):
                    # Abort was requested while waiting. We should exit
                    break

Player().ServiceEntryPoint()