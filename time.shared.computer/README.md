

[cpu]:docs/time_shared_cpu.png
[events]: docs/events.png


## Simulation of Time Shared Computer Model
Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
Example 2.5 page 129 Time-Shared Computer Model

(Code is NOT ready yet)

A company has a computer system consisting of a single central processing unit (TimeSharedComputer) and n terminals, as shown if Fig. 2.13.The operator of each terminal “think” for an amount of time that is an exponential random variable with mean 25 seconds, and then sends to the TimeSharedComputer a job having service time distributed exponentially with mean 0.8 seconds.

![cpu]

Arriving jobs join a single queue for the TimeSharedComputer but are served in a round-robin rather than FIFO manner. That is, the TimeSharedComputer allocates each job a maximum quantum
of length q = 0.1 second. If the (remaining) service time of a job, s seconds, is no more than q, the TimeSharedComputer spends s seconds, plus a fixed swap time of T=0.015 seconds, processing the job, which then return to its terminal. However, if s > q, the TimeSharedComputer spends q + t seconds processing the job, which then joins the end of the queue, and its remaining service time is decremented by q seconds.
This process is repeated until the job's service is eventually completed, at which point it returns to its terminal, whose operator begins another think time.
Let R_i be teh response time of the ith job to finish service, which is defined as the time elapsing between the instant the job leaves its terminal and the instant it is finished being processed at the TimeSharedComputer. For each of the cases n = 10, 20,..., 80, we simulate the computer system for 1000 job completions (respose times) and estimate the expected average response time of these jobs, the expected time-average number of jobs waiting in the queue, and the expected utilization of the TimeSharedComputer. Assume that all terminal are in the think state at time 0. The company would like to know how many terminals  it can have on its system and still provide users with an average response time of no more than 30 seconds.

The events for this model are:

| Event description        | Event enum type |
| ------------- |-------------------:|
| Arrival of a job to the TimeSharedComputer from a terminal      | JOB_ARRIVAL|
| End of TimeSharedComputer run      | END_CPU_RUN|
| End of Simulation | END_OF_SIMULATION|

//TO be continued

![events]
