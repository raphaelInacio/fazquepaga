export interface Task {
    id?: string;
    description?: string;
    type: 'DAILY' | 'WEEKLY' | 'ONE_TIME';
    weight: 'LOW' | 'MEDIUM' | 'HIGH';
    status?: 'PENDING' | 'COMPLETED' | 'PENDING_APPROVAL' | 'APPROVED';
    requiresProof: boolean;
    createdAt?: string;
    dayOfWeek?: number;
    scheduledDate?: string;
    aiValidated?: boolean;
}

export interface CreateTaskRequest {
    description: string;
    type: 'DAILY' | 'WEEKLY' | 'ONE_TIME';
    weight: 'LOW' | 'MEDIUM' | 'HIGH';
    requiresProof: boolean;
    dayOfWeek?: number;
    scheduledDate?: string;
}

export interface User {
    id?: string;
    name: string;
    email?: string;
    role?: 'PARENT' | 'CHILD';
    parentId?: string;
    phoneNumber?: string;
    monthlyAllowance?: number;
    age?: number;
}

export interface CreateChildRequest {
    name: string;
    parentId: string;
    phoneNumber: string;
    age?: number;
}

export interface ChildWithLocalData extends User {
    id: string;
    name: string;
    age: number;
    phoneNumber: string;
}

export interface CreateParentRequest {
    name: string;
    email: string;
}
